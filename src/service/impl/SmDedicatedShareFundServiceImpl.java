package service.impl;

import constant.InitConstant.OverbookFactor;
import constant.InitConstant.Global;
import service.IDedicatedShareShareFundService;

import java.util.List;

/**
 * @author Sunyuejun
 */
public class SmDedicatedShareFundServiceImpl implements IDedicatedShareShareFundService {
    private final double[] idealShard, dedicated, lastRewardSum;
    private final List<List<Integer>> load, rewardMax;
    private final List<List<Double>> rewardMin;
    private final int rewardPool;
    private final double[][] overFactor;

    private double rewardSmPool;

    private final double[] result;

    private SmDedicatedShareFundServiceImpl(Builder builder) {
        this.load = builder.load;
        this.rewardMax = builder.rewardMax;
        this.rewardPool = builder.rewardPool;
        this.rewardMin = builder.rewardMin;
        this.lastRewardSum = builder.lastRewardSum;

        this.idealShard = new double[Global.CITY_NUM];
        this.overFactor = new double[Global.CITY_NUM][builder.timeWindowLen];
        this.dedicated = new double[Global.CITY_NUM];
        this.result = new double[Global.CITY_NUM];
    }


    public double getRewardSmPool() {
        return rewardSmPool;
    }

    public double[] getDedicated() {
        return dedicated;
    }

    @Override
    public void calculateOverFactor(int t) {
        for (int city = 0; city < Global.CITY_NUM; ++city) {
            double lastOverFactor = t == 0 ? OverbookFactor.INIT : overFactor[city][t - 1];
            if (load.get(city).get(t) * lastOverFactor < lastRewardSum[city]) {
                overFactor[city][t] = 1;
            } else {
                overFactor[city][t] = OverbookFactor.SM;
            }
        }
    }

    @Override
    public double calculateDedicated(int city, int t) {
        return Math.min(overFactor[city][t] * load.get(city).get(t), rewardMin.get(city).get(t));
    }

    @Override
    public double calculateIdealShard(int city, int t) {
        dedicated[city] = calculateDedicated(city, t);
        double result = Math.max(0,
                Math.min(
                        overFactor[city][t] * load.get(city).get(t) - dedicated[city],
                        rewardMax.get(city).get(t) - rewardMin.get(city).get(t)));
        idealShard[city] = result;
        return result;
    }

    @Override
    public double[] calculateFund(int t) {
        calculateOverFactor(t);
        for (int city = 0; city < Global.CITY_NUM; ++city) {
            double totalIdealSharedSm = 0;
            rewardSmPool = rewardPool;
            for (int i = 0; i < Global.CITY_NUM; ++i) {
                rewardSmPool -= rewardMin.get(i).get(t);
                totalIdealSharedSm += calculateIdealShard(i, t);
            }
            result[city] = Math.min(idealShard[city], (idealShard[city] / totalIdealSharedSm) * rewardSmPool);
        }
        return result;
    }


    public static class Builder {
        private List<List<Integer>> load, rewardMax;
        private int rewardPool, timeWindowLen;
        private List<List<Double>> rewardMin;
        private double[] lastRewardSum;

        public Builder() {
            this.rewardPool = 0;
            this.timeWindowLen = 0;
            this.load = null;
            this.rewardMax = null;
            this.rewardMin = null;
            this.lastRewardSum = null;
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

        public Builder setLastRewardSum(double[] lastRewardSum) {
            this.lastRewardSum = lastRewardSum;
            return this;
        }

        public SmDedicatedShareFundServiceImpl build() {
            return new SmDedicatedShareFundServiceImpl(this);
        }
    }


}
