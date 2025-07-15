package com.codewithmosh;

import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.system.MemoryStack;

import java.nio.*;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class ChessGame {
    private long window;
    private final int WIDTH = 800;
    private final int HEIGHT = 800;
    private final int GRID_SIZE = 5;
    private final float CELL_SIZE = 100.0f;
    private final float MARGIN = 50.0f; // 边缘留白

    // 游戏状态
    private boolean redTurn = true;
    private int diceValue = 0;
    private boolean gameOver = false;

    // 棋子位置
    private int[][] redPieces = new int[6][2];  // [棋子编号][x,y]
    private int[][] bluePieces = new int[6][2]; // [棋子编号][x,y]
    private boolean[] redActive = new boolean[6];
    private boolean[] blueActive = new boolean[6];

    public static void main(String[] args) {
        new ChessGame().run();
    }

    public void run() {
        System.out.println("LWJGL版本: " + Version.getVersion());

        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("无法初始化GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        // 使用OpenGL 2.1兼容模式
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        window = glfwCreateWindow(WIDTH, HEIGHT, "爱恩斯坦棋", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("无法创建GLFW窗口");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);

            if (!gameOver && action == GLFW_RELEASE) {
                if (key == GLFW_KEY_SPACE) {
                    rollDice();
                }

                // 选择移动方向
                if (diceValue > 0) {
                    if (key == GLFW_KEY_RIGHT || key == GLFW_KEY_D) {
                        moveInDirection(redTurn ? 0 : 0); // 红方向右，蓝方向左
                    } else if (key == GLFW_KEY_DOWN || key == GLFW_KEY_S) {
                        moveInDirection(redTurn ? 1 : 1); // 红方向下，蓝方向上
                    } else if (key == GLFW_KEY_PAGE_DOWN) {
                        moveInDirection(redTurn ? 2 : 2); // 红方向右下，蓝方向左上
                    }
                }
            }
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        // 设置2D渲染
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, WIDTH, HEIGHT, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // 启用混合
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        initializeGame();
    }

    private void initializeGame() {
        // 初始化红方棋子（左上角）
        redPieces[0] = new int[]{0, 0}; // 1号棋子
        redPieces[1] = new int[]{1, 0}; // 2号棋子
        redPieces[2] = new int[]{0, 1}; // 3号棋子
        redPieces[3] = new int[]{1, 1}; // 4号棋子
        redPieces[4] = new int[]{0, 2}; // 5号棋子
        redPieces[5] = new int[]{2, 0}; // 6号棋子

        // 初始化蓝方棋子（右下角）
        bluePieces[0] = new int[]{4, 4}; // 1号棋子
        bluePieces[1] = new int[]{3, 4}; // 2号棋子
        bluePieces[2] = new int[]{4, 3}; // 3号棋子
        bluePieces[3] = new int[]{3, 3}; // 4号棋子
        bluePieces[4] = new int[]{4, 2}; // 5号棋子
        bluePieces[5] = new int[]{2, 4}; // 6号棋子

        // 设置所有棋子为活跃状态
        for (int i = 0; i < 6; i++) {
            redActive[i] = true;
            blueActive[i] = true;
        }
    }

    private void rollDice() {
        Random random = new Random();
        diceValue = random.nextInt(6) + 1;
        System.out.println((redTurn ? "红方" : "蓝方") + "掷出了 " + diceValue);

        // 检查是否有对应编号的棋子
        boolean hasPiece = false;
        for (int i = 0; i < 6; i++) {
            if ((i + 1 == diceValue) && (redTurn? redActive[i] : blueActive[i])) {
                hasPiece = true;
                break;
            }
        }

        if (!hasPiece) {
            System.out.println("没有可移动的棋子，轮到对方。");
            redTurn = !redTurn; // 切换回合
            diceValue = 0;
        }
    }

    private void moveInDirection(int direction) {
        // 根据方向移动棋子
        int[][] pieces = redTurn ? redPieces : bluePieces;
        boolean[] active = redTurn ? redActive : blueActive;

        for (int i = 0; i < 6; i++) {
            if ((i + 1 == diceValue) && active[i]) {
                int[] newPos = new int[]{pieces[i][0], pieces[i][1]};

                // 计算新位置
                if (redTurn) {
                    // 红方：右、下、右下
                    switch (direction) {
                        case 0: newPos[0]++; break; // 右
                        case 1: newPos[1]++; break; // 下
                        case 2: newPos[0]++; newPos[1]++; break; // 右下
                    }
                } else {
                    // 蓝方：左、上、左上
                    switch (direction) {
                        case 0: newPos[0]--; break; // 左
                        case 1: newPos[1]--; break; // 上
                        case 2: newPos[0]--; newPos[1]--; break; // 左上
                    }
                }

                // 检查是否在棋盘范围内
                if (newPos[0] >= 0 && newPos[0] < GRID_SIZE &&
                        newPos[1] >= 0 && newPos[1] < GRID_SIZE) {

                    // 检查是否有对方棋子
                    boolean captured = false;
                    int[][] otherPieces = redTurn ? bluePieces : redPieces;
                    boolean[] otherActive = redTurn ? blueActive : redActive;

                    for (int j = 0; j < 6; j++) {
                        if (otherActive[j] && otherPieces[j][0] == newPos[0] && otherPieces[j][1] == newPos[1]) {
                            captured = true;
                            otherActive[j] = false; // 捕获对方棋子
                            System.out.println((redTurn ? "红方" : "蓝方") + "捕获了对方的棋子！");
                            break;
                        }
                    }

                    // 移动棋子
                    pieces[i][0] = newPos[0];
                    pieces[i][1] = newPos[1];
                    System.out.println((redTurn ? "红方" : "蓝方") + "移动了棋子到 (" + newPos[0] + ", " + newPos[1] + ")");
                    break;
                } else {
                    System.out.println("移动超出棋盘范围！");
                }
            }
        }

        // 切换回合
        redTurn = !redTurn;
        diceValue = 0;
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            render();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void render() {
        // 渲染棋盘
        glBegin(GL_QUADS);
        glColor3f(0.5f, 0.5f, 0.5f);
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                glVertex2f(i * CELL_SIZE + MARGIN, j * CELL_SIZE + MARGIN);
                glVertex2f((i + 1) * CELL_SIZE + MARGIN, j * CELL_SIZE + MARGIN);
                glVertex2f((i + 1) * CELL_SIZE + MARGIN, (j + 1) * CELL_SIZE + MARGIN);
                glVertex2f(i * CELL_SIZE + MARGIN, (j + 1) * CELL_SIZE + MARGIN);
            }
        }
        glEnd();

        // 渲染棋子
        renderPieces(redPieces, redActive, new float[]{1.0f, 0.0f, 0.0f}); // 红色
        renderPieces(bluePieces, blueActive, new float[]{0.0f, 0.0f, 1.0f}); // 蓝色
    }

    private void renderPieces(int[][] pieces, boolean[] active, float[] color) {
        glColor3f(color[0], color[1], color[2]);
        for (int i = 0; i < pieces.length; i++) {
            if (active[i]) {
                glBegin(GL_QUADS);
                glVertex2f(pieces[i][0] * CELL_SIZE + MARGIN, pieces[i][1] * CELL_SIZE + MARGIN);
                glVertex2f((pieces[i][0] + 1) * CELL_SIZE + MARGIN, pieces[i][1] * CELL_SIZE + MARGIN);
                glVertex2f((pieces[i][0] + 1) * CELL_SIZE + MARGIN, (pieces[i][1] + 1) * CELL_SIZE + MARGIN);
                glVertex2f(pieces[i][0] * CELL_SIZE + MARGIN, (pieces[i][1] + 1) * CELL_SIZE + MARGIN);
                glEnd();
            }
        }
    }
}