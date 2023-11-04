package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.utils.debug.ObjectInspector;

import java.util.List;
import java.util.StringJoiner;

class ClassGenerator {

    static String generate(String name, List<ObjectInspector.Slot> slots) {
        String template =
                "import {import};\n" +
                "\n" +
                "public class {className} extends {updatable} {\n" +
                "\n" +
                "    {members};\n" +
                "\n" +
                "    @Override\n" +
                "    public void update() {\n" +
                "        {membersRead};\n" +
                "    }\n" +
                "}";
        template = template.replace("{className}", name);
        template = template.replace("{import}", Updatable.class.getName());
        template = template.replace("{updatable}", Updatable.class.getSimpleName());

        StringJoiner members = new StringJoiner(";\n    ");
        StringJoiner membersRead = new StringJoiner(";\n        ");

        for (ObjectInspector.Slot slot : slots) {
            String slotName = slot.name.replace("-", "");
            String read = "this." + slotName + " = ";
            switch (slot.slotType) {
                case INT:
                case UINT:
                case BOOLEAN:
                    members.add("private int " + slotName);
                    read += "readInt";
                    break;
                case DOUBLE:
                    members.add("private double " + slotName);
                    read += "readDouble";
                    break;
                case STRING:
                    members.add("private String " + slotName);
                    read += "readString";
                    break;
                default:
                    members.add("private long " + slotName);
                    read += "readLong";
            }
            membersRead.add(read + "(" + slot.offset + ")");
        }

        template = template.replace("{members}", members.toString());
        template = template.replace("{membersRead}", membersRead.toString());
        return template;
    }
}
