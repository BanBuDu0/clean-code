package service.impl;

import constant.InitConstant.OverbookFactor;
import constant.InitConstant.Global;
import service.IDedicatedShareShareFundService;

import java.util.List;

import static constant.InitConstant.Global.CITY_NUM;

/**
 * @author Sunyuejun
 */
public class DisDedicatedShareFundServiceImpl implements IDedicatedShareShareFundService {
    private static final DisDedicatedShareFundServiceImpl INSTANCE = new DisDedicatedShareFundServiceImpl();
    private List<List<Integer>> load, rewardMax;
    private List<List<Double>> rewardMin;
    private double rewardSmPool;
    private double[] dedicatedSms, sharedSms;
    private int[] lastRewardSum;

    private double[][] overFactor = null;
    private final double[] idealShard = new double[CITY_NUM];
    private final double[] result = new double[CITY_NUM];
    private double rewardDisPool;


    public static DisDedicatedShareFundServiceImpl getInstance() {
        return INSTANCE;
    }


    private void setParam(Builder builder) {
        this.load = builder.load;
        this.rewardMax = builder.rewardMax;
        this.rewardSmPool = builder.rewardSmPool;
        this.rewardMin = builder.rewardMin;
        this.dedicatedSms = builder.dedicatedSms;
        this.sharedSms = builder.sharedSms;
        this.lastRewardSum = builder.lastRewardSum;
        if (null == overFactor) {
            this.overFactor = new double[CITY_NUM][builder.timeWindowLen];
        }
    }


    public double getRewardDisPool() {
        return rewardDisPool;
    }


    @Override
    public void calculateOverFactor(int t) {
        for (int city = 0; city < Global.CITY_NUM; ++city) {
            double lastOverFactor = t == 0 ? OverbookFactor.INIT : overFactor[city][t - 1];
            if (load.get(city).get(t) * lastOverFactor < lastRewardSum[city]) {
                overFactor[city][t] = 1;
            } else {
                overFactor[city][t] = OverbookFactor.DIS;
            }
        }
    }

    @Override
    public double calculateDedicated(int city, int t) {
        return Math.min(overFactor[city][t] * load.get(city).get(t), rewardMin.get(city).get(t) - dedicatedSms[city]);
    }

    @Override
    public double calculateIdealShard(int city, int t) {
        double result = Math.max(0,
                Math.min(
                        overFactor[city][t] * load.get(city).get(t) - calculateDedicated(city, t),
                        rewardMax.get(city).get(t) - sharedSms[city]));
        idealShard[city] = result;
        return result;
    }

    @Override
    public double[] calculateFund(int t) {
        calculateOverFactor(t);
        for (int city = 0; city < Global.CITY_NUM; ++city) {
            double totalIdealSharedDis = 0;
            rewardDisPool = this.rewardSmPool;
            for (int i = 0; i < Global.CITY_NUM; ++i) {
                rewardDisPool -= sharedSms[i];
                totalIdealSharedDis += calculateIdealShard(i, t);
            }
            if (idealShard[city] == 0) {
                result[city] = 0;
            } else {
                result[city] = Math.min(idealShard[city], (idealShard[city] / totalIdealSharedDis) * rewardDisPool);
            }
        }
        return result;
    }


    public static class Builder {
        private List<List<Integer>> load, rewardMax;
        private List<List<Double>> rewardMin;
        private double rewardSmPool;
        private int timeWindowLen;
        private double[] dedicatedSms, sharedSms;
        private int[] lastRewardSum;
        private final DisDedicatedShareFundServiceImpl disDedicatedShareFundService
                = DisDedicatedShareFundServiceImpl.getInstance();

        public Builder() {
            this.rewardSmPool = 0;
            this.timeWindowLen = 0;
            this.rewardMax = null;
            this.load = null;
            this.rewardMin = null;
            this.dedicatedSms = null;
            this.sharedSms = null;
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

        public Builder setLastRewardSum(int[] lastRewardSum) {
            this.lastRewardSum = lastRewardSum;
            return this;
        }

        public DisDedicatedShareFundServiceImpl build() {
            disDedicatedShareFundService.setParam(this);
            return disDedicatedShareFundService;
        }
    }

}
