import constant.InitConstant;
import service.IMinimumFundService;
import service.impl.DisSharedFundServiceImpl;
import service.impl.MinimumFundServiceImpl;
import service.impl.SmSharedFundServiceImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static constant.InitConstant.Global.CITY_NUM;


/**
 * @author Sunyuejun
 */
public class Main {
    private int rewardPool = 0, poolMinRate = 0;
    private final List<List<Integer>> rewardMax = new ArrayList<>();
    private final List<List<Integer>> loadSm = new ArrayList<>();
    private final List<List<Integer>> loadDis = new ArrayList<>();
    private final List<List<Integer>> loadOther = new ArrayList<>();
    private int timeWindowLen = 0;

    private IMinimumFundService minimumFundService;
    private SmSharedFundServiceImpl smSharedFundService;
    private DisSharedFundServiceImpl disSharedFundService;

    private final List<List<Double>> rewardMin = new ArrayList<>();
    private final List<List<Double>> rewardShardSm = new ArrayList<>();
    private final List<List<Double>> rewardShardDis = new ArrayList<>();
    private final List<List<Double>> rewardShardOther = new ArrayList<>();
    private double[] lastRewardSum = new double[CITY_NUM];


    private void init() {
        for (int i = 0; i < CITY_NUM; ++i) {
            rewardMax.add(new ArrayList<>());
            loadSm.add(new ArrayList<>());
            loadDis.add(new ArrayList<>());
            loadOther.add(new ArrayList<>());
            rewardMin.add(new ArrayList<>());
            rewardShardSm.add(new ArrayList<>());
            rewardShardDis.add(new ArrayList<>());
            rewardShardOther.add(new ArrayList<>());
        }
    }

    private String readData(String fileName) {
        try {
            File file = new File("src/" + fileName);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(read);
                // Read the pool parameter in the first line
                String poolParameter = bufferedReader.readLine();
                if (poolParameter != null) {
                    String[] poolParameters = poolParameter.split(",");
                    rewardPool = Integer.parseInt(poolParameters[0]);
                    poolMinRate = Integer.parseInt(poolParameters[1]);
                }
                // Read the city requirements parameter
                String lineTxt;
                do {
                    for (int i = 0; i < CITY_NUM; ++i) {
                        if ((lineTxt = bufferedReader.readLine()) != null) {
                            String[] cityRequires = lineTxt.split(",");
                            rewardMax.get(i).add(Integer.parseInt(cityRequires[0]));
                            loadSm.get(i).add(Integer.parseInt(cityRequires[1]));
                            loadDis.get(i).add(Integer.parseInt(cityRequires[2]));
                            loadOther.get(i).add(Integer.parseInt(cityRequires[3]));
                        }
                    }
                    lineTxt = bufferedReader.readLine();
                } while (lineTxt != null);
                read.close();
                timeWindowLen = rewardMax.get(0).size();
            } else {
                return "找不到指定的文件";
            }
        } catch (Exception e) {
            return "读取文件内容出错";
        }
        return null;
    }

    private void calculateRewardMin(int t) {
        minimumFundService = new MinimumFundServiceImpl.Builder()
                .setRewardPool(rewardPool)
                .setPoolMinRate(poolMinRate)
                .setRewardMax(rewardMax)
                .build();
        for (int city = 0; city < CITY_NUM; ++city) {
            rewardMin.get(city).add(minimumFundService.calculateRewardMin(city, t));
        }
    }

    private void calculateSmSharedFund(int t) {
        smSharedFundService = new SmSharedFundServiceImpl.Builder()
                .setLoad(loadSm)
                .setRewardMax(rewardMax)
                .setRewardMin(rewardMin)
                .setRewardPool(rewardPool)
                .setTimeWindowLen(timeWindowLen)
                .build();

        for (int city = 0; city < CITY_NUM; ++city) {
            double resultOfRewardShardSm = smSharedFundService.calculateShared(city, t, lastRewardSum[city]);
            rewardShardSm.get(city).add(resultOfRewardShardSm);
        }
    }

    private void calculateDisSharedFund(int t) {
        double[] tempShardSm = new double[CITY_NUM];
        for (int city = 0; city < CITY_NUM; ++city) {
            tempShardSm[city] = rewardShardSm.get(city).get(t);
        }
        disSharedFundService = new DisSharedFundServiceImpl.Builder()
                .setLoad(loadDis)
                .setRewardMax(rewardMax)
                .setTimeWindowLen(timeWindowLen)
                .setRewardMin(rewardMin)
                .setDedicatedSms(smSharedFundService.getDedicated())
                .setSharedSms(tempShardSm)
                .setRewardSmPool(smSharedFundService.getRewardSmPool())
                .build();

        for (int city = 0; city < CITY_NUM; ++city) {
            double resultOfRewardShardDis = disSharedFundService.calculateShared(city, t, lastRewardSum[city]);
            rewardShardDis.get(city).add(resultOfRewardShardDis);
        }
    }


    private void calculate() {
        for (int t = 0; t < timeWindowLen; ++t) {
            calculateRewardMin(t);
            calculateSmSharedFund(t);
            calculateDisSharedFund(t);

            // TODO
            for (int city = 0; city < CITY_NUM; ++city) {
                lastRewardSum[city] = rewardMin.get(city).get(t) + rewardShardSm.get(city).get(t)
                        + rewardShardDis.get(city).get(t) + rewardShardOther.get(city).get(t);
            }

        }

    }


    public static void main(String[] args) {
        // write your code here
        if (args.length < 1) {
            return;
        }
        Main main = new Main();
        main.init();
        String error = main.readData(args[0]);
        if (error != null) {
            System.out.println(error);
            return;
        }
        main.calculate();

    }
}
