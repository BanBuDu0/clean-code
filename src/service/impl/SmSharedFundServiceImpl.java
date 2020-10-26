package service.impl;

import constant.InitConstant.OverbookFactor;
import constant.InitConstant.Global;
import constant.InitConstant.InitOverbookFactor;
import service.ISharedFundService;

import java.util.List;

/**
 * @author Sunyuejun
 */
public class SmSharedFundServiceImpl implements ISharedFundService {
    private final double[] idealShard, overFactor, dedicated;
    private final List<List<Integer>> load, rewardMax;
    private final List<List<Double>> rewardMin;
    private final int rewardPool;

    private double rewardSmPool;

    private SmSharedFundServiceImpl(Builder builder) {
        this.load = builder.load;
        this.rewardMax = builder.rewardMax;
        this.rewardPool = builder.rewardPool;
        this.rewardMin = builder.rewardMin;

        this.idealShard = new double[Global.CITY_NUM];
        this.overFactor = new double[builder.timeWindowLen];
        this.dedicated = new double[Global.CITY_NUM];
    }


    public double getRewardSmPool() {
        return rewardSmPool;
    }

    public double[] getDedicated() {
        return dedicated;
    }

    @Override
    public void calculateOverFactor(int city, int t, double lastRewardSum) {
        double lastOverFactor = t == 0 ? InitOverbookFactor.SM : overFactor[t - 1];
        if (load.get(city).get(t) * lastOverFactor < lastRewardSum) {
            overFactor[t] = 1;
        } else {
            overFactor[t] = OverbookFactor.SM;
        }
    }

    @Override
    public double calculateDedicated(int city, int t) {
        return Math.min(overFactor[t] * load.get(city).get(t), rewardMin.get(city).get(t));
    }

    @Override
    public double calculateIdealShard(int city, int t) {
        dedicated[city] = calculateDedicated(city, t);
        double result = Math.max(0,
                Math.min(
                        overFactor[t] * load.get(city).get(t) - dedicated[city],
                        rewardMax.get(city).get(t) - rewardMin.get(city).get(t)));
        idealShard[city] = result;
        return result;
    }

    @Override
    public double calculateShared(int city, int t, double lastRewardSum) {
        calculateOverFactor(city, t, lastRewardSum);
        double totalIdealSharedSm = 0;
        rewardSmPool = rewardPool;
        for (int i = 0; i < Global.CITY_NUM; ++i) {
            rewardSmPool -= rewardMin.get(i).get(t);
            totalIdealSharedSm += calculateIdealShard(i, t);
        }

        return Math.min(idealShard[city], (idealShard[city] / totalIdealSharedSm) * rewardSmPool);
    }

    public static class Builder {
        private List<List<Integer>> load, rewardMax;
        private int rewardPool, timeWindowLen;
        private List<List<Double>> rewardMin;

        public Builder() {
            this.rewardPool = 0;
            this.timeWindowLen = 0;
            this.load = null;
            this.rewardMax = null;
            this.rewardMin = null;
        }

        public Builder setLoad(List<List<Integer>> load) {
            this.load = load;
            return this;
        }

        public Builder setRewardMax(List<List<Integer>> rewardMax) {
            this.rewardMax = rewardMax;
            return this;
        }

        public Builder setRewardPool(int rewardPool) {
            this.rewardPool = rewardPool;
            return this;
        }

        public Builder setTimeWindowLen(int timeWindowLen) {
            this.timeWindowLen = timeWindowLen;
            return this;
        }

        public Builder setRewardMin(List<List<Double>> rewardMin) {
            this.rewardMin = rewardMin;
            return this;
        }

        public SmSharedFundServiceImpl build() {
            return new SmSharedFundServiceImpl(this);
        }
    }


}
