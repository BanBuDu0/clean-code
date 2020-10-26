package service;

/**
 * @author Sunyuejun
 */
public interface ISharedFundService {

    /**
     * calculate Over Factor
     *
     * @param city          city index
     * @param t             time slot
     * @param lastRewardSum lastRewardSum
     */
    void calculateOverFactor(int city, int t, double lastRewardSum);

    /**
     * calculate Dedicated
     *
     * @param city city index
     * @param t    time slot
     * @return Dedicated
     */
    double calculateDedicated(int city, int t);

    /**
     * calculate Ideal Shard
     *
     * @param city city index
     * @param t    time slot
     * @return IdealShard
     */
    double calculateIdealShard(int city, int t);

    /**
     * calculate Shared
     *
     * @param city          city index
     * @param t             time slot
     * @param lastRewardSum lastRewardSum
     * @return Shared
     */
    double calculateShared(int city, int t, double lastRewardSum);
}
