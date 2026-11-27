package com.github.manolo8.darkbot.behaviours.memory;

import com.github.manolo8.darkbot.core.objects.facades.ItemsControlMenuMediator;
import com.github.manolo8.darkbot.core.utils.AvmUtils;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.util.Timer;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.concurrent.TimeUnit;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Releases action bar elements that the client threw away but still keeps through their tooltip.
 * <p>
 * Every action bar element registers a tooltip hook on creation. The bar is rebuilt as a whole many times per session,
 * and the old elements are dropped without unregistering, so the tooltip control keeps every element ever shown,
 * with its whole display tree, until the client reloads.
 * <p>
 * An element is stale once the bar's mediator no longer lists it. Stale elements are found three ways, each covering
 * what the others can't see:
 * <ul>
 *     <li>an element that was on the bar on the previous pass and left it, through its own hook</li>
 *     <li>the hooks added to the tooltip control's list since the previous pass, which also finds the elements
 *     dropped before they were ever seen on the bar. A list seen for the first time is read from its start, so the
 *     elements dropped before the cleaner started are found too</li>
 *     <li>a slow round over the whole list, for the few new hooks a change of the list (a hook removed before the
 *     ones already read) moved out of sight</li>
 * </ul>
 * Each way reads a bounded number of hooks per pass and keeps no position that a change of the list could break.
 * An element still stale after {@link #STALE_NS} gets its hook's target set to null, which frees it. The hook handles
 * a null target (it is checked or caught everywhere) and stays in the list as a small shell.
 */
class TooltipCleaner implements LeakCleaner {

    /** Static of the tooltip control class holding its instance. */
    private static final int CONTROL_CLASS_INSTANCE = 0x30;
    /** Tooltip control: its tooltip hooks (Array). */
    private static final int CONTROL_HOOKS = 0x20;
    /** Tooltip hook: the object it shows the tooltip of. */
    private static final int HOOK_TARGET = 0x30;
    /** Action bar element: its own tooltip hook. */
    private static final int ELEMENT_HOOK = 0x1A0;

    private static final long INTERVAL_MS = 10_000;
    /** Time an element stays off the bar before it is released: not one that is just being built or moved. */
    private static final long STALE_NS = TimeUnit.SECONDS.toNanos(15);
    /** Searching the tooltip control's class goes through every class of the client: a few times per client. */
    private static final long CLOSURE_SEARCH_INTERVAL_MS = 30_000;
    /**
     * Searches per client. The class is created when the client first uses it, so it may be missing right after the
     * client loads; one still not found after these won't be found in that client.
     */
    private static final int CLOSURE_SEARCHES_PER_CLIENT = 10;
    /** The hooks of the newest elements are the last ones of the list: how many are checked to recognize it. */
    private static final int RECENT_HOOKS = 256;
    /** New hooks read per pass: far above what a pass adds; a long list seen for the first time takes a few passes. */
    private static final int NEW_HOOKS_PER_PASS = 1_024;
    /**
     * Hooks checked per pass by the slow round over the list: as many as the list grew by, so the round always gets
     * ahead of a growing list, plus a share of the list that ends a round in about {@code ROUND_PASSES} passes,
     * at least {@code ROUND_HOOKS_PER_PASS} and at most {@code MAX_ROUND_HOOKS_PER_PASS}.
     */
    private static final int ROUND_HOOKS_PER_PASS = 64, MAX_ROUND_HOOKS_PER_PASS = 1_024, ROUND_PASSES = 90;
    /** A GUI rebuild after a map change can drop thousands of elements at once. */
    private static final int MAX_RELEASES_PER_PASS = 4_096;
    /** Stale elements waiting at most: far above any rebuild, more means values that aren't action bar elements. */
    private static final int MAX_STALE = 16_384;
    /** Passes in a row with elements on the bar but none of their hooks found, before it is reported. */
    private static final int HOOK_MISSES_REPORTED = 6;

    private final ItemsControlMenuMediator mediator;
    private final Timer closureSearch = Timer.get(CLOSURE_SEARCH_INTERVAL_MS);
    /** Elements on the bar now, and on the previous pass. */
    private LongSet liveElements = new LongOpenHashSet(), previousElements = new LongOpenHashSet();
    /** Classes of the elements on the bar, and of their hooks: learned from the elements as they appear. */
    private final LongSet elementClasses = new LongOpenHashSet(), hookClasses = new LongOpenHashSet();
    /** Stale elements not released yet: element -> its hook, and element -> since when it is stale. */
    private final Long2LongOpenHashMap staleHooks = new Long2LongOpenHashMap(), staleSince = new Long2LongOpenHashMap();

    /** The tooltip control's class, and the searches left in this client. */
    private long controlClass;
    private int searchesLeft = CLOSURE_SEARCHES_PER_CLIENT;
    /** The hook list followed, the first of its hooks not read yet, and the next one of the slow round. */
    private long hookList;
    private int newHooksIndex, roundIndex;
    private long released;
    private int hookMisses;
    private boolean listMissingReported, hooksMissingReported, tooManyStaleReported;

    TooltipCleaner(ItemsControlMenuMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public long getIntervalMs() {
        return INTERVAL_MS;
    }

    @Override
    public long getReleased() {
        return released;
    }

    @Override
    public String getReleasedName() {
        return "action bar elements";
    }

    @Override
    public void reset() {
        // The control's class, the searches made and the places in its list are kept: they belong to the client
        liveElements.clear();
        previousElements.clear();
        elementClasses.clear();
        hookClasses.clear();
        staleHooks.clear();
        staleSince.clear();
        staleHooks.trim();
        staleSince.trim();
        hookMisses = 0;
    }

    @Override
    public void clientChanged() {
        reset();
        controlClass = 0;
        searchesLeft = CLOSURE_SEARCHES_PER_CLIENT;
        closureSearch.disarm();
        hookList = 0;
        newHooksIndex = roundIndex = 0;
    }

    /** Releases the stale elements. Does nothing while the elements on the bar or their hooks can't be read. */
    @Override
    public void clean() {
        if (!readLiveElements()) return;
        long now = System.nanoTime();
        collectDroppedElements(now);
        readHookList(now);
        releaseStaleElements(now);

        LongSet swap = previousElements;
        previousElements = liveElements;
        liveElements = swap;
    }

    /**
     * Reads the elements on the bar, and learns the classes of the new ones and of their hooks. False if they can't
     * be read, there are none, or no hook of theirs points back to its element: then nothing may be released.
     */
    private boolean readLiveElements() {
        liveElements.clear();
        if (!mediator.readElements(liveElements) || liveElements.isEmpty()) return false;

        for (LongIterator it = liveElements.iterator(); it.hasNext(); ) {
            long element = it.nextLong();
            if (!previousElements.contains(element)) learnClasses(element);
        }
        if (!hookClasses.isEmpty()) {
            hookMisses = 0;
            return true;
        }
        if (++hookMisses >= HOOK_MISSES_REPORTED) reportMissingHooks();
        return false;
    }

    private void learnClasses(long element) {
        elementClasses.add(AvmUtils.vtableOf(element));
        long hook = API.readLong(element + ELEMENT_HOOK);
        if (hook != 0 && API.readLong(hook + HOOK_TARGET) == element) hookClasses.add(AvmUtils.vtableOf(hook));
    }

    /** Elements that were on the bar on the previous pass and left it: stale, found through their own hook. */
    private void collectDroppedElements(long now) {
        for (LongIterator it = previousElements.iterator(); it.hasNext(); ) {
            long element = it.nextLong();
            if (!liveElements.contains(element)) addStale(element, API.readLong(element + ELEMENT_HOOK), now);
        }
    }

    /**
     * Checks the hooks added to the list since the previous pass, then the next hooks of the slow round over it.
     * A shorter list had hooks removed: reading goes on from its end, and the round finds what the change hid.
     */
    private void readHookList(long now) {
        long list = hookList();
        if (list == 0) return;
        long atoms = AvmUtils.arrayAtoms(list);
        int length = AvmUtils.arrayLength(list);
        if (atoms == 0 || length <= 0) return;
        if (list != hookList) {
            hookList = list;
            newHooksIndex = roundIndex = 0;
        }

        newHooksIndex = Math.min(newHooksIndex, length);
        int end = Math.min(length, newHooksIndex + NEW_HOOKS_PER_PASS);
        for (int i = newHooksIndex; i < end; i++) checkHook(AvmUtils.atomAt(atoms, i), now);
        int added = end - newHooksIndex;
        newHooksIndex = end;

        int share = Math.min(MAX_ROUND_HOOKS_PER_PASS, Math.max(ROUND_HOOKS_PER_PASS, length / ROUND_PASSES));
        for (int checked = Math.min(added + share, length); checked > 0; checked--) {
            if (roundIndex >= length) roundIndex = 0;
            checkHook(AvmUtils.atomAt(atoms, roundIndex++), now);
        }
    }

    /** Remembers the target of the hook if it is a stale action bar element. */
    private void checkHook(long hook, long now) {
        if (hook == 0) return;
        long target = API.readLong(hook + HOOK_TARGET);
        // Released hooks have no target, and the other hooks are of objects other than bar elements
        if (target == 0 || liveElements.contains(target) || staleSince.containsKey(target)) return;
        if (elementClasses.contains(AvmUtils.vtableOf(target))) addStale(target, hook, now);
    }

    /** Remembers a stale element and its hook, checked to be a tooltip hook of that element. */
    private void addStale(long element, long hook, long now) {
        if (hook == 0 || staleSince.containsKey(element)) return;
        if (!hookClasses.contains(AvmUtils.vtableOf(hook)) || API.readLong(hook + HOOK_TARGET) != element) return;
        if (staleSince.size() >= MAX_STALE) {
            reportTooManyStale();
            return;
        }
        staleHooks.put(element, hook);
        staleSince.put(element, now);
    }

    /**
     * Releases the elements stale for long enough, by setting their hook's target to null if it still is the
     * element. An element back on the bar is no longer stale.
     */
    private void releaseStaleElements(long now) {
        int releasedNow = 0;
        ObjectIterator<Long2LongMap.Entry> it = staleSince.long2LongEntrySet().iterator();
        while (it.hasNext()) {
            Long2LongMap.Entry entry = it.next();
            long element = entry.getLongKey();
            if (liveElements.contains(element)) {
                staleHooks.remove(element);
                it.remove();
                continue;
            }
            if (now - entry.getLongValue() < STALE_NS || releasedNow >= MAX_RELEASES_PER_PASS) continue;

            API.replaceLong(staleHooks.remove(element) + HOOK_TARGET, element, 0);
            it.remove();
            releasedNow++;
        }
        released += releasedNow;
    }

    /** The tooltip hook list, read from the control's instance on every pass (it is created on first use). */
    private long hookList() {
        if (controlClass == 0 && !searchControlClass()) return 0;
        long control = API.readAtom(controlClass + CONTROL_CLASS_INSTANCE);
        return control == 0 ? 0 : API.readAtom(control + CONTROL_HOOKS);
    }

    /**
     * Searches the tooltip control's class, at most {@link #CLOSURE_SEARCHES_PER_CLIENT} times per client: the class
     * whose instance's hook list ends with the hook of an element on the bar. Recognized by what it holds rather
     * than by name, like the bot finds other classes.
     */
    private boolean searchControlClass() {
        if (searchesLeft == 0 || closureSearch.isActive()) return false;
        searchesLeft--;
        closureSearch.activate();

        long hook = liveElementHook();
        if (hook != 0) controlClass = API.searchClassClosure(closure -> holdsHook(closure, hook));
        if (controlClass == 0 && searchesLeft == 0) reportMissingList();
        return controlClass != 0;
    }

    /** The tooltip hook of an element on the bar, checked to point back to it: 0 if none. */
    private long liveElementHook() {
        for (LongIterator it = liveElements.iterator(); it.hasNext(); ) {
            long element = it.nextLong();
            long hook = API.readLong(element + ELEMENT_HOOK);
            if (hook != 0 && API.readLong(hook + HOOK_TARGET) == element) return hook;
        }
        return 0;
    }

    /** Whether the class' instance has a hook list whose last hooks include the hook. */
    private static boolean holdsHook(long closure, long hook) {
        long control = API.readAtom(closure + CONTROL_CLASS_INSTANCE);
        if (!ByteUtils.isScriptableObjectValid(control)) return false;
        long list = API.readAtom(control + CONTROL_HOOKS);
        if (!ByteUtils.isScriptableObjectValid(list)) return false;
        int length = AvmUtils.arrayLength(list);
        long atoms = AvmUtils.arrayAtoms(list);
        if (length <= 0 || atoms == 0) return false;
        for (int i = Math.max(0, length - RECENT_HOOKS); i < length; i++)
            if (AvmUtils.atomAt(atoms, i) == hook) return true;
        return false;
    }

    private void reportMissingList() {
        if (listMissingReported) return;
        listMissingReported = true;
        System.out.println("Memory cleaner: the tooltip hook list was not found, the offsets may be outdated: " +
                "only elements seen on the bar are released until the client reloads");
    }

    private void reportMissingHooks() {
        if (hooksMissingReported) return;
        hooksMissingReported = true;
        System.out.println("Memory cleaner: the tooltip hooks of the action bar elements were not found, " +
                "the offsets may be outdated: action bar elements are not released");
    }

    private void reportTooManyStale() {
        if (tooManyStaleReported) return;
        tooManyStaleReported = true;
        System.out.println("Memory cleaner: more than " + MAX_STALE + " stale action bar elements at once, " +
                "the offsets may be outdated: the others wait for a later pass");
    }
}
