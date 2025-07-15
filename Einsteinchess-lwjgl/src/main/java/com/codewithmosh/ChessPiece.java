package com.codewithmosh;

/**
 * 棋子类
 * 负责棋子的属性和渲染
 */
public class ChessPiece {
    private String name; // 棋子名称
    private int x; // 棋子X坐标
    private int y; // 棋子Y坐标

    /**
     * 构造函数
     */
    public ChessPiece(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    /**
     * 渲染棋子
     */
    public void render() {
        // 渲染棋子的逻辑
        // 这里可以使用OpenGL绘制棋子的形状
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
