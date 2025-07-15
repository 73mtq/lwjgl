package com.codewithmosh;

import java.util.Random;

/**
 * 骰子类
 * 负责掷骰子的逻辑
 */
public class Dice {
    private Random random;

    /**
     * 构造函数
     */
    public Dice() {
        random = new Random();
    }

    /**
     * 掷骰子
     */
    public int roll() {
        return random.nextInt(6) + 1; // 返回1到6的随机数
    }
}
