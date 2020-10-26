package service.impl;

import service.INonDedicatedShareFundService;

import java.util.List;

import static constant.InitConstant.Global.CITY_NUM;
import static constant.InitConstant.OverbookFactor;

/**
 * @author Sunyuejun
 */
public class OtherSharedFundServiceImpl implements INonDedicatedShareFundService {
    private static final OtherSharedFundServiceImpl INSTANCE = new OtherSharedFundServiceImpl();
    private List<List<Integer>> load, rewardMax;
    private List<List<Double>> rewardMin, rewardShardSm, rewardShardDis;
    private double[][] overFactor = null;
    private double rewardDisPool;
    private double[] sharedDis;
    private final double[] result = new double[CITY_NUM];


    public static OtherSharedFundServiceImpl getInstance() {
        return INSTANCE;
    }

    private void setParam(Builder builder) {
        this.rewardMax = builder.rewardMax;
        this.rewardMin = builder.rewardMin;
        this.rewardShardSm = builder.rewardShardSm;
        this.rewardShardDis = builder.rewardShardDis;
        this.rewardDisPool = builder.rewardDisPool;
        this.sharedDis = builder.sharedDis;
        this.load = builder.load;
        if (null == overFactor) {
            overFactor = new double[CITY_NUM][builder.timeWindowLen];
        }
    }

    @Override
    public void calculateOverFactor(int t) {
        int term = t % CITY_NUM;
        for (int city = 0; city < CITY_NUM; ++city) {
            if (city == term) {
                overFactor[city][t] = OverbookFactor.OTHER;
            } else {
                overFactor[city][t] = OverbookFactor.INIT;
            }
        }
    }

    @Override
    public double calculateIdealShard(int city, int t) {
        return rewardMax.get(city).get(t) - rewardMin.get(city).get(t) -
                rewardShardSm.get(city).get(t) - rewardShardDis.get(city).get(t);
    }

    @Override
    public double[] calculateFund(int t) {
        calculateOverFactor(t);

        double rewardOfOtherPool = rewardDisPool, totalOtherDis = 0;
        for (int city = 0; city < CITY_NUM; ++city) {
            rewardOfOtherPool -= sharedDis[city];
            totalOtherDis += load.get(city).get(t) * overFactor[city][t];
        }
        for (int city = 0; city < CITY_NUM; ++city) {
            result[city] = Math.min(calculateIdealShard(city, t),
                    (load.get(city).get(t) * overFactor[city][t] * rewardOfOtherPool) / totalOtherDis);
        }
        return result;
    }


    public static class Builder {
        private List<List<Integer>> load, rewardMax;
        private List<List<Double>> rewardMin, rewardShardSm, rewardShardDis;
        private double rewardDisPool;
        private double[] sharedDis;
        private int timeWindowLen;
        private final OtherSharedFundServiceImpl otherSharedFundService = OtherSharedFundServiceImpl.getInstance();

        public Builder() {
            this.load = null;
            this.rewardMax = null;
            this.rewardMin = null;
            this.rewardShardSm = null;
            this.rewardShardDis = null;
            this.timeWindowLen = 0;
            this.rewardDisPool = 0;
            this.sharedDis = null;
        }

        public Builder setTimeWindowLen(int timeWindowLen) {
            this.timeWindowLen = timeWindowLen;
            return this;
        }

        public Builder setLoad(List<List<Integer>> load) {
            this.load = load;
            return this;
        }

        public Builder setRewardMax(List<List<Integer>> rewardMax) {
            this.rewardMax = rewardMax;
            return this;
        }

        public Builder setRewardMin(List<List<Double>> rewardMin) {
            this.rewardMin = rewardMin;
            return this;
        }

        public Builder setRewardShardSm(List<List<Double>> rewardShardSm) {
            this.rewardShardSm = rewardShardSm;
            return this;
        }

        public Builder setRewardShardDis(List<List<Double>> rewardShardDis) {
            this.rewardShardDis = rewardShardDis;
            return this;
        }

        public Builder setRewardDisPool(double rewardDisPool) {
            this.rewardDisPool = rewardDisPool;
            return this;
        }

        public Builder setSharedDis(double[] sharedDis) {
            this.sharedDis = sharedDis;
            return this;
        }

        public OtherSharedFundServiceImpl build() {
            otherSharedFundService.setParam(this);
            return otherSharedFundService;
        }

    }
}
