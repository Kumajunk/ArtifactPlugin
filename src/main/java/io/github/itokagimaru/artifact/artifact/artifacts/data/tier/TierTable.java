package io.github.itokagimaru.artifact.artifact.artifacts.data.tier;

public class TierTable {
    public static enum dropRate{
        HIGH(0.4),
        MIDDLE(0.35),
        LOW(0.25);
        public final double getRate;
        dropRate(double rate){
            this.getRate = rate;
        }
    }
    public static enum dropRateLv{
        lv0(0, Tier.artifactTier.C, Tier.artifactTier.C, Tier.artifactTier.C),
        lv1(1, Tier.artifactTier.C, Tier.artifactTier.C, Tier.artifactTier.B),
        lv2(2, Tier.artifactTier.C, Tier.artifactTier.B, Tier.artifactTier.A),
        lv3(3, Tier.artifactTier.B, Tier.artifactTier.A, Tier.artifactTier.S),
        lv4(4, Tier.artifactTier.A, Tier.artifactTier.S, Tier.artifactTier.SS),
        lv5(5, Tier.artifactTier.S, Tier.artifactTier.SS, Tier.artifactTier.SS),
        lv6(6, Tier.artifactTier.SS, Tier.artifactTier.SS, Tier.artifactTier.SS);
        public final int getId;
        public final Tier.artifactTier[] getDropTier;
        dropRateLv(int id, Tier.artifactTier higt, Tier.artifactTier middle, Tier.artifactTier low){
            this.getId = id;
            this.getDropTier = new Tier.artifactTier[]{higt,middle,low};
        }
    }
}
