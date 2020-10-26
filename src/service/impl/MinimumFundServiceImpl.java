package service.impl;

import constant.InitConstant.Global;
import service.IMinimumFundService;

import java.util.List;

/**
 * @author Sunyuejun
 */
public class MinimumFundServiceImpl implements IMinimumFundService {
    private final int rewardPool, poolMinRate;
    private final List<List<Integer>> rewardMax;

    public MinimumFundServiceImpl(int rewardPool, int poolMinRate, List<List<Integer>> rewardMax) {
        this.rewardPool = rewardPool;
        this.poolMinRate = poolMinRate;
        this.rewardMax = rewardMax;
    }

    @Override
    public double calculateRewardMin(int city, int t) {
        int sumOfRewardMax = 0;
        for (int i = 0; i < Global.CITY_NUM; ++i) {
            sumOfRewardMax += rewardMax.get(city).get(t);
        }
        return Math.min(
                Math.ceil(rewardPool * ((double) poolMinRate / 100) * ((double) rewardMax.get(city).get(t) / sumOfRewardMax)),
                rewardMax.get(city).get(t));
    }

    public static class Builder {
        private int rewardPool, poolMinRate;
        private List<List<Integer>> rewardMax;

        public Builder() {
            this.rewardPool = 0;
            this.poolMinRate = 0;
            this.rewardMax = null;
        }

        public Builder setRewardPool(int rewardPool) {
            this.rewardPool = rewardPool;
            return this;
        }

        public Builder setPoolMinRate(int poolMinRate) {
            this.poolMinRate = poolMinRate;
            return this;
        }

        public Builder setRewardMax(List<List<Integer>> rewardMax) {
            this.rewardMax = rewardMax;
            return this;
        }

        public MinimumFundServiceImpl build() {
            return new MinimumFundServiceImpl(rewardPool, poolMinRate, rewardMax);
        }
    }

}
