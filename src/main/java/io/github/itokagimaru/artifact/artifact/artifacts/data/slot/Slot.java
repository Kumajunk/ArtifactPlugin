package io.github.itokagimaru.artifact.artifact.artifacts.data.slot;

import java.util.HashMap;

public class Slot {
    public static enum artifactSlot {
        PEAR(0, "PearCut"),//HP
        OVAL(1, "OvalCut"),//ATK
        LOZENGE(2, "LozengeCut"),//CRI,CRIDMG,ATK,HP,DEF
        CLOVER(3, "CloverCut"),//ATK,HP,LUK,DEF,VIT
        CUSHION(4, "CushionCut"),//HP,DEF,VIT
        CRESCENT(5, "CrescentCut");//FIRE,WATER,NATURE,ATK,DEF
        public final String getSlotName;
        public final int getId;
        artifactSlot(int id, String slot){
            this.getSlotName = slot;
            this.getId = id;
        }

        public static final HashMap<Integer,artifactSlot> slotHashMap = new HashMap<>();
        public static int slotSize = 0;
        static {
            for (artifactSlot slot : values()){
                slotHashMap.put(slot.getId,slot);
                slotSize++;
            }
        }
        public static artifactSlot fromId(int value){
            return slotHashMap.get(value);
        }
    }
}
