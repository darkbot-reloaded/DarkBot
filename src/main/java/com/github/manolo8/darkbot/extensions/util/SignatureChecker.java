package com.github.manolo8.darkbot.extensions.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class SignatureChecker {

    private static final String META_INF = "META-INF/";
    private static final String SIG_PREFIX = META_INF + "SIG-";
    private static final KnownCerts KNOWN_CERTS = new KnownCerts();

    public static Boolean verifyJar(JarFile jf) throws IOException {
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

        if (man == null) return null;
        Enumeration<JarEntry> e = entriesVec.elements();

        // Used to cache allowed certs, no longer needing to check pub byte array for them
        Set<Certificate> allowedCerts = new HashSet<>();

        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            String name = je.getName();
            if (je.isDirectory() || signatureRelated(name)) continue;

            Boolean signed = checkCertificates(je.getCertificates(), allowedCerts);
            if (signed == null) return null;
            if (!signed) return false;
        }
        return true;
    }

    private static Boolean checkCertificates(Certificate[] certs, Set<Certificate> allowedCerts) {
        if (certs == null || certs.length == 0) return null;
        for (Certificate cert : certs) {
            if (allowedCerts.contains(cert)) return true;
            ByteArray bytes = new ByteArray(cert.getPublicKey().getEncoded());
            if (KNOWN_CERTS.test(bytes)){
                 allowedCerts.add(cert);
                return true;
            }
        }
        return false;
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
