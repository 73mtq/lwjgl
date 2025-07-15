package com.codewithmosh;

import java.util.ArrayList;
import java.util.List;

/**
 * 棋盘类
 * 负责管理棋盘状态和棋子的渲染
 */
public class ChessBoard {
    private final int SIZE = 8; // 棋盘大小
    private ChessPiece[][] board; // 棋盘数组
    private List<ChessPiece> redPieces; // 红方棋子列表
    private List<ChessPiece> bluePieces; // 蓝方棋子列表

    /**
     * 初始化棋盘
     */
    public void initializeBoard(Player redPlayer, Player bluePlayer) {
        board = new ChessPiece[SIZE][SIZE];
        redPieces = new ArrayList<>();
        bluePieces = new ArrayList<>();

        // 初始化红方棋子
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < SIZE; j++) {
                ChessPiece piece = new ChessPiece("红方棋子", i, j);
                redPieces.add(piece);
                board[i][j] = piece;
            }
        }

        // 初始化蓝方棋子
        for (int i = SIZE - 1; i >= SIZE - 2; i--) {
            for (int j = 0; j < SIZE; j++) {
                ChessPiece piece = new ChessPiece("蓝方棋子", i, j);
                bluePieces.add(piece);
                board[i][j] = piece;
            }
        }
    }

    /**
     * 渲染棋盘
     */
    public void render() {
        // 渲染棋盘的逻辑
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != null) {
                    board[i][j].render();
                }
            }
        }
    }
}
