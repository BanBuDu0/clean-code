package service.impl;

import constant.InitConstant;
import service.ISharedFundService;

import java.util.List;

/**
 * @author Sunyuejun
 */
public class DisSharedFundServiceImpl implements ISharedFundService {
    private final double[] idealShard, overFactor;
    private final List<List<Integer>> load, rewardMax;
    private final List<List<Double>> rewardMin;
    private final double rewardSmPool;
    private final double[] dedicatedSms, sharedSms;

    private DisSharedFundServiceImpl(Builder builder) {
        this.load = builder.load;
        this.rewardMax = builder.rewardMax;
        this.rewardSmPool = builder.rewardSmPool;
        this.rewardMin = builder.rewardMin;
        this.dedicatedSms = builder.dedicatedSms;
        this.sharedSms = builder.sharedSms;

        this.idealShard = new double[InitConstant.Global.CITY_NUM];
        this.overFactor = new double[builder.timeWindowLen];
    }


    @Override
    public void calculateOverFactor(int city, int t, double lastRewardSum) {
        double lastOverFactor = t == 0 ? InitConstant.InitOverbookFactor.DIS : overFactor[t - 1];
        if (load.get(city).get(t) * lastOverFactor < lastRewardSum) {
            overFactor[t] = 1;
        } else {
            overFactor[t] = InitConstant.OverbookFactor.DIS;
        }
    }

    @Override
    public double calculateDedicated(int city, int t) {
        return Math.min(overFactor[t] * load.get(city).get(t), rewardMin.get(city).get(t) - dedicatedSms[city]);
    }

    @Override
    public double calculateIdealShard(int city, int t) {
        double result = Math.max(0,
                Math.min(
                        overFactor[t] * load.get(city).get(t) - calculateDedicated(city, t),
                        rewardMax.get(city).get(t) - sharedSms[city]));
        idealShard[city] = result;
        return result;
    }

    @Override
    public double calculateShared(int city, int t, double lastRewardSum) {
        calculateOverFactor(city, t, lastRewardSum);
        double totalIdealSharedDis = 0, rewardDisPool = this.rewardSmPool;
        for (int i = 0; i < InitConstant.Global.CITY_NUM; ++i) {
            rewardDisPool -= sharedSms[i];
            totalIdealSharedDis += calculateIdealShard(i, t);
        }

        return Math.min(idealShard[city], (idealShard[city] / totalIdealSharedDis) * rewardDisPool);
    }

    public static class Builder {
        private List<List<Integer>> load, rewardMax;
        private List<List<Double>> rewardMin;
        private double rewardSmPool;
        private int timeWindowLen;
        private double[] dedicatedSms, sharedSms;

        public Builder() {
            this.rewardSmPool = 0;
            this.timeWindowLen = 0;
            this.rewardMax = null;
            this.load = null;
            this.rewardMin = null;
            this.dedicatedSms = null;
            this.sharedSms = null;
        }

        // TODO build 这里换成单例模式

        public Builder setLoad(List<List<Integer>> load) {
            this.load = load;
            return this;
        }

        public Builder setRewardMax(List<List<Integer>> rewardMax) {
            this.rewardMax = rewardMax;
            return this;
        }

        public Builder setRewardSmPool(double rewardSmPool) {
            this.rewardSmPool = rewardSmPool;
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

        public Builder setDedicatedSms(double[] dedicatedSms) {
            this.dedicatedSms = dedicatedSms;
            return this;
        }

        public Builder setSharedSms(double[] sharedSms) {
            this.sharedSms = sharedSms;
            return this;
        }

        public DisSharedFundServiceImpl build() {
            return new DisSharedFundServiceImpl(this);
        }
    }

}
