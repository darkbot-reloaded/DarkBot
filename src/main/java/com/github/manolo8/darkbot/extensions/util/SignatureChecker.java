package com.github.manolo8.darkbot.extensions.util;

import sun.security.util.SignatureFileVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSigner;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class SignatureChecker {

    private static final String META_INF = "META-INF/";
    private static final String SIG_PREFIX = META_INF + "SIG-";


    public static boolean verifyJar(JarFile jf) throws IOException {
        Vector<JarEntry> entriesVec = new Vector<>();
        byte[] buffer = new byte[8192];

        Enumeration<JarEntry> entries = jf.entries();
        while (entries.hasMoreElements()) {
            JarEntry je = entries.nextElement();
            entriesVec.addElement(je);

            try (InputStream is = jf.getInputStream(je)) {
                //noinspection StatementWithEmptyBody
                while (is.read(buffer, 0, buffer.length) != -1);
                // we just read. this will throw a SecurityException
                // if  a signature/digest check fails.
            }
        }

        Manifest man = jf.getManifest();

        if (man == null) return false;
        Enumeration<JarEntry> e = entriesVec.elements();

        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            String name = je.getName();
            CodeSigner[] signers = je.getCodeSigners();

            boolean isSigned = (signers != null);
            if (!je.isDirectory() && !isSigned && !signatureRelated(name)) {
                return false;
            }

            // TODO: check if the signature is known and accepted.
            /*int inStoreOrScope = inKeyStore(signers);

            boolean inStore = (inStoreOrScope & IN_KEYSTORE) != 0;
            boolean inScope = (inStoreOrScope & IN_SCOPE) != 0;

            notSignedByAlias |= (inStoreOrScope & NOT_ALIAS) != 0;
            if (keystore != null) {
                aliasNotInStore |= isSigned && (!inStore && !inScope);
            }*/
        }

        return true;
    }

    /**
     * signature-related files include:
     * . META-INF/MANIFEST.MF
     * . META-INF/SIG-*
     * . META-INF/*.SF
     * . META-INF/*.DSA
     * . META-INF/*.RSA
     * . META-INF/*.EC
     */
    private static boolean signatureRelated(String name) {
        String ucName = name.toUpperCase(Locale.ENGLISH);
        if (ucName.equals(JarFile.MANIFEST_NAME) ||
                ucName.equals(META_INF) ||
                (ucName.startsWith(SIG_PREFIX) &&
                        ucName.indexOf("/") == ucName.lastIndexOf("/"))) {
            return true;
        }

        if (ucName.startsWith(META_INF) && isBlockOrSF(ucName)) {
            // .SF/.DSA/.RSA/.EC files in META-INF subdirs
            // are not considered signature-related
            return (ucName.indexOf("/") == ucName.lastIndexOf("/"));
        }

        return false;
    }

    private static boolean isBlockOrSF(String var0) {
        return var0.endsWith(".SF") || var0.endsWith(".DSA") || var0.endsWith(".RSA") || var0.endsWith(".EC");
    }


}
