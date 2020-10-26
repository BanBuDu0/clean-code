package service.impl;

import service.INonDedicatedShareFundService;

import java.util.List;

import static constant.InitConstant.Global.CITY_NUM;
import static constant.InitConstant.OverbookFactor;

/**
 * @author Sunyuejun
 */
public class OtherSharedFundServiceImpl implements INonDedicatedShareFundService {
    private List<List<Integer>> rewardMax, rewardMin, rewardShardSm, rewardShardDis;
    private List<List<Integer>> load;
    private final double[][] overFactor;
    private double[] result;
    private double rewardDisPool;
    private double[] sharedDiss;

    public OtherSharedFundServiceImpl(Builder builder) {
        this.rewardMax = builder.rewardMax;
        this.rewardMin = builder.rewardMin;
        this.rewardShardSm = builder.rewardShardSm;
        this.rewardShardDis = builder.rewardShardDis;

        this.overFactor = new double[CITY_NUM][builder.timeWindowLen];
        this.result = new double[CITY_NUM];
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
            rewardOfOtherPool -= sharedDiss[city];
            totalOtherDis += load.get(city).get(t) * overFactor[city][t];
        }
        for (int city = 0; city < CITY_NUM; ++city) {
            result[city] = Math.min(calculateIdealShard(city, t),
                    (load.get(city).get(t) * overFactor[city][t] * rewardOfOtherPool) / totalOtherDis);
        }
        return result;
    }


    public static class Builder {
        private List<List<Integer>> rewardMax, rewardMin, rewardShardSm, rewardShardDis;
        private int timeWindowLen;


    }
}
