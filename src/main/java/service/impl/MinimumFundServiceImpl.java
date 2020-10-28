package service.impl;

import constant.InitConstant.Global;
import service.IBaseFundService;

import java.util.List;

/**
 * @author Sunyuejun
 */
public class MinimumFundServiceImpl implements IBaseFundService {
    private static final MinimumFundServiceImpl INSTANCE = new MinimumFundServiceImpl();
    private int rewardPool, poolMinRate;
    private List<List<Integer>> rewardMax;
    private final double[] result = new double[Global.CITY_NUM];

    public static MinimumFundServiceImpl getInstance() {
        return INSTANCE;
    }

    private void setParam(Builder builder) {
        this.rewardPool = builder.rewardPool;
        this.poolMinRate = builder.poolMinRate;
        this.rewardMax = builder.rewardMax;
    }

    @Override
    public double[] calculateFund(int t) {
        for (int city = 0; city < Global.CITY_NUM; ++city) {
            int sumOfRewardMax = 0;
            for (int i = 0; i < Global.CITY_NUM; ++i) {
                sumOfRewardMax += rewardMax.get(i).get(t);
            }
            if (rewardMax.get(city).get(t) == 0) {
                result[city] = 0;
            } else {
                result[city] = Math.min(Math.ceil(rewardPool * ((double) poolMinRate / 100) *
                        ((double) rewardMax.get(city).get(t) / sumOfRewardMax)), rewardMax.get(city).get(t));
            }
        }
        return result;
    }


    public static class Builder {
        private int rewardPool, poolMinRate;
        private List<List<Integer>> rewardMax;
        private final MinimumFundServiceImpl minimumFundService = MinimumFundServiceImpl.getInstance();

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

            minimumFundService.setParam(this);

            return minimumFundService;
        }
    }

}
