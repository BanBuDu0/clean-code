package service;

/**
 * @author Sunyuejun
 */
public interface IMinimumFundService {
    /**
     * 计算城市city在时刻t分配的最小资金
     *
     * @param city city index
     * @param t    time slot
     * @return 城市city在时刻t分配的最小资金
     */
    double calculateRewardMin(int city, int t);
}
