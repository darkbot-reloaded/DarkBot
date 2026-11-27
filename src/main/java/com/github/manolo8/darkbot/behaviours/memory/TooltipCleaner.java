package com.github.manolo8.darkbot.behaviours.memory;

import com.github.manolo8.darkbot.core.manager.FacadeManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.util.Timer;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.behaviours.memory.AvmMemory.*;

/**
 * Releases action bar elements that the client threw away but still keeps through their tooltip.
 * <p>
 * Every action bar element registers a tooltip hook on creation. The bar is rebuilt as a whole many times per session,
 * and the old elements are dropped without unregistering, so the tooltip singleton keeps every element ever shown,
 * with its whole display tree, until the client reloads.
 * <p>
 * An element is stale when the bar's mediator no longer lists it. A hook whose element was stale on two
 * consecutive passes gets its target set to null, which frees the element. The hook itself handles a null target
 * (it is checked or caught everywhere) and stays in the list as a small shell.
 * <p>
 * The hook list only grows, and a hook's target never changes. So each hook is read once, when it is added: only
 * those whose target is an action bar element are watched, until their element is released. What is kept stays at
 * the size of the action bar, however long the session. If the list changes before the hooks already read
 * (a hook removed or inserted), it is read again from the start.
 */
class TooltipCleaner implements LeakCleaner {

    private static final String TOOLTIP_CONTROL_CLASS = "TooltipControl$";
    private static final String BAR_ELEMENT_CLASS = "SlotAndItemBarElement";
    private static final String SINGLETON = "_instance";
    private static final String TOOLTIP_LIST = "toolTipsList";
    private static final String HOOK_TARGET = "targetObject";
    private static final String CATEGORY_ELEMENTS = "_categoryItemsMap";
    private static final String SLOT_ELEMENTS = "_slots2itemsMap";

    private static final long INTERVAL_MS = 15_000;
    /** Searching a class closure scans the client memory: a few searches per client, at this interval. */
    private static final long CLOSURE_SEARCH_INTERVAL_MS = 30_000;
    /**
     * Searches per client. A class is created when the client first uses it, so it may be missing right after the
     * client loads; one still missing after these was renamed by a client update.
     */
    private static final int CLOSURE_SEARCHES_PER_CLIENT = 10;
    /** New hooks read per pass: far above what a pass adds, a long list read from the start takes a few passes. */
    private static final int HOOKS_PER_PASS = 16_384;
    private static final int MAX_RELEASES_PER_PASS = 500;

    private final SlotResolver slots;
    private final FacadeManager facadeManager;
    private final Timer closureSearch = Timer.get(CLOSURE_SEARCH_INTERVAL_MS);
    private final ListReader hooks = new ListReader(ListReader.Layout.ARRAY, HOOKS_PER_PASS);
    private final DictionaryReader elementMap = new DictionaryReader();
    private final LongSet liveElements = new LongOpenHashSet();
    private final LongSet checkedClasses = new LongOpenHashSet(), barElementClasses = new LongOpenHashSet();
    /** Hooks whose target is an action bar element, with that target: the only hooks read again. */
    private final Long2LongOpenHashMap watched = new Long2LongOpenHashMap();
    /** Watched hooks found stale on the previous pass. */
    private LongSet stale = new LongOpenHashSet(), nextStale = new LongOpenHashSet();

    /** The hook list followed, how many of its hooks were read, and the last of them. */
    private long hookList, lastHook;
    private int hooksRead;
    /** The tooltip class closure, the VM core of the client it was searched in, and searches left there. */
    private long closure, searchedCore;
    private int searchesLeft;
    private boolean closureMissingReported;

    TooltipCleaner(SlotResolver slots, FacadeManager facadeManager) {
        this.slots = slots;
        this.facadeManager = facadeManager;
    }

    @Override
    public long getIntervalMs() {
        return INTERVAL_MS;
    }

    @Override
    public void reset() {
        // The closure and the searches made are kept until the client changes: searching is expensive
        checkedClasses.clear();
        barElementClasses.clear();
        hooks.release();
        elementMap.release();
        followList(0);
        watched.trim();
    }

    /** Releases the elements of stale hooks. Does nothing if the hooks or the live elements can't be read. */
    @Override
    public void clean() {
        long list = slots.readRequired(tooltipControl(), TOOLTIP_LIST);
        if (list == 0 || !readLiveElements() || !readNewHooks(list)) return;
        releaseStaleElements();
    }

    /**
     * The tooltip singleton, or 0 if not found (yet). It is created on first use, so it is read from its class
     * closure on every pass, which takes two reads.
     */
    private long tooltipControl() {
        return hasClosure() || searchClosure() ? slots.readRequired(closure, SINGLETON) : 0;
    }

    /** A class closure lives as long as its client: the one found stays valid until the client changes. */
    private boolean hasClosure() {
        return closure != 0 && searchedCore == slots.getCore();
    }

    /** Searches the closure, at most {@link #CLOSURE_SEARCHES_PER_CLIENT} times per client. */
    private boolean searchClosure() {
        closure = 0;
        long core = slots.getCore();
        if (core != searchedCore) {
            searchedCore = core;
            searchesLeft = CLOSURE_SEARCHES_PER_CLIENT;
            closureSearch.disarm();
        }
        if (searchesLeft == 0 || closureSearch.isActive()) return false;

        searchesLeft--;
        closure = API.searchClassClosure(this::isTooltipClosure);
        if (closure != 0) return true;
        closureSearch.activate();
        if (searchesLeft == 0) reportMissingClosure();
        return false;
    }

    private boolean isTooltipClosure(long closure) {
        return ByteUtils.isScriptableObjectValid(closure) &&
                TOOLTIP_CONTROL_CLASS.equals(ByteUtils.readObjectName(closure));
    }

    /** Elements the action bar shows now. False if the bar's mediator or its maps can't be read. */
    private boolean readLiveElements() {
        long mediator = facadeManager.itemsControlMediator.address;
        if (mediator == 0) return false;

        liveElements.clear();
        return addKeys(mediator, CATEGORY_ELEMENTS) && addKeys(mediator, SLOT_ELEMENTS);
    }

    /**
     * Adds the keys of a dictionary field of the mediator. A null dictionary means no elements (the bar was never
     * built). A field that can't be resolved means the client changed, and a dictionary that can't be read may
     * hide live elements: in both cases nothing may be released.
     */
    private boolean addKeys(long mediator, String field) {
        if (!slots.require(mediator, field)) return false;
        long dictionary = slots.readPointer(mediator, field);
        if (dictionary == 0) return true;
        if (!elementMap.read(dictionary)) return false;
        for (int i = 0; i < elementMap.size(); i++) liveElements.add(elementMap.key(i));
        return true;
    }

    /**
     * Reads the hooks added to the list since the last pass, and watches those of action bar elements. A new list,
     * or one changed before the hooks already read, is followed from the start. False if the list can't be read now.
     */
    private boolean readNewHooks(long list) {
        if (list != hookList || (hooksRead > 0 && hooks.element(list, hooksRead - 1) != lastHook)) followList(list);
        if (!hooks.readFrom(list, hooksRead)) {
            if (hooks.length() < hooksRead) followList(list); // shorter: hooks were removed, read it again next pass
            return false;
        }
        for (int i = 0; i < hooks.count(); i++) watchIfBarElement(hooks.get(i));
        hooksRead += hooks.count();
        if (hooks.count() > 0) lastHook = hooks.get(hooks.count() - 1);
        return true;
    }

    /** Starts following the list from its first hook, forgetting the hooks of the previous one. */
    private void followList(long list) {
        hookList = list;
        hooksRead = 0;
        lastHook = 0;
        watched.clear();
        stale.clear();
    }

    private void watchIfBarElement(long hook) {
        if (hook == 0) return; // hole in the array
        long targetOffset = slots.referenceOffset(hook, HOOK_TARGET);
        if (targetOffset < 0) {
            slots.requireReference(hook, HOOK_TARGET); // reports a renamed or retyped field once
            return;
        }
        long target = API.readAtom(hook + targetOffset);
        // Hooks of other elements, and already released ones, never need to be read again
        if (target != 0 && isBarElement(target)) watched.put(hook, target);
    }

    /** Releases the elements of watched hooks that were stale on two passes in a row, and stops watching them. */
    private void releaseStaleElements() {
        nextStale.clear();
        int released = 0;
        ObjectIterator<Long2LongMap.Entry> it = watched.long2LongEntrySet().iterator();
        while (it.hasNext()) {
            Long2LongMap.Entry entry = it.next();
            long hook = entry.getLongKey(), target = entry.getLongValue();
            if (liveElements.contains(target)) continue;

            long targetOffset = slots.referenceOffset(hook, HOOK_TARGET);
            // No longer a hook of this element: nothing left to watch
            if (targetOffset < 0 || API.readAtom(hook + targetOffset) != target) {
                it.remove();
                continue;
            }
            // Stale twice in a row: not an element that is just being built
            if (stale.contains(hook) && released < MAX_RELEASES_PER_PASS) {
                API.writeLong(hook + targetOffset, 0);
                it.remove();
                released++;
            } else {
                nextStale.add(hook);
            }
        }
        LongSet swap = stale;
        stale = nextStale;
        nextStale = swap;
    }

    private void reportMissingClosure() {
        if (closureMissingReported) return;
        closureMissingReported = true;
        System.out.println("Memory cleaner: no class " + TOOLTIP_CONTROL_CLASS +
                ", the client changed: action bar elements are not released until it reloads");
    }

    private boolean isBarElement(long object) {
        long vtable = API.readLong(object + OBJECT_VTABLE);
        if (checkedClasses.add(vtable) && BAR_ELEMENT_CLASS.equals(ByteUtils.readObjectName(object))) {
            barElementClasses.add(vtable);
        }
        return barElementClasses.contains(vtable);
    }
}
