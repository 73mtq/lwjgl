package com.codewithmosh;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GridRenderer {

    private long window;
    private int shaderProgram;
    private int vao;
    private int vbo;

    // 窗口大小
    private final int WIDTH = 800;
    private final int HEIGHT = 800;

    // 棋盘网格大小
    private final int GRID_SIZE = 5;

    public void run() {
        init();
        loop();

        // 释放资源
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteProgram(shaderProgram);

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // 初始化GLFW
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("无法初始化GLFW");

        // 配置GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // 创建窗口
        window = glfwCreateWindow(WIDTH, HEIGHT, "5×5网格棋盘", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("无法创建GLFW窗口");

        // 设置键盘回调
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        // 获取线程栈并压入一个新的帧
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            // 获取窗口大小
            glfwGetWindowSize(window, pWidth, pHeight);

            // 获取显示器信息
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // 将窗口居中
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        // 使OpenGL上下文成为当前线程的当前上下文
        glfwMakeContextCurrent(window);
        // 启用垂直同步
        glfwSwapInterval(1);
        // 显示窗口
        glfwShowWindow(window);
    }

    private void loop() {
        // 创建GL上下文
        GL.createCapabilities();

        // 设置清屏颜色
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

        // 编译着色器
        compileShaders();

        // 创建网格数据
        createGridMesh();

        // 渲染循环
        while (!glfwWindowShouldClose(window)) {
            // 清除颜色缓冲区
            glClear(GL_COLOR_BUFFER_BIT);

            // 使用着色器程序
            glUseProgram(shaderProgram);

            // 绑定VAO
            glBindVertexArray(vao);

            // 绘制网格线
            glDrawArrays(GL_LINES, 0, (GRID_SIZE + 1) * 4);

            // 解绑VAO
            glBindVertexArray(0);

            // 交换缓冲区
            glfwSwapBuffers(window);

            // 轮询事件
            glfwPollEvents();
        }
    }

    private void compileShaders() {
        // 顶点着色器
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader,
                "#version 330 core\n" +
                        "layout (location = 0) in vec2 position;\n" +
                        "void main() {\n" +
                        "    gl_Position = vec4(position, 0.0, 1.0);\n" +
                        "}");
        glCompileShader(vertexShader);

        // 片段着色器
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader,
                "#version 330 core\n" +
                        "out vec4 fragColor;\n" +
                        "void main() {\n" +
                        "    fragColor = vec4(0.0, 0.0, 0.0, 1.0);\n" +  // 黑色网格线
                        "}");
        glCompileShader(fragmentShader);

        // 链接着色器程序
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        // 删除着色器，它们已经链接到程序中，不再需要
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private void createGridMesh() {
        // 创建VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // 创建VBO
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // 计算网格单元大小
        float cellSize = 2.0f / GRID_SIZE;

        // 创建顶点数据 - 存储水平和垂直线的坐标
        float[] vertices = new float[(GRID_SIZE + 1) * 4 * 2]; // (行数+列数)*2点*2坐标

        int index = 0;

        // 水平线
        for (int i = 0; i <= GRID_SIZE; i++) {
            float y = -1.0f + i * cellSize;

            // 从左到右的线
            vertices[index++] = -1.0f;        // 左端点x
            vertices[index++] = y;            // 左端点y
            vertices[index++] = 1.0f;         // 右端点x
            vertices[index++] = y;            // 右端点y
        }

        // 垂直线
        for (int i = 0; i <= GRID_SIZE; i++) {
            float x = -1.0f + i * cellSize;

            // 从上到下的线
            vertices[index++] = x;            // 上端点x
            vertices[index++] = -1.0f;        // 上端点y
            vertices[index++] = x;            // 下端点x
            vertices[index++] = 1.0f;         // 下端点y
        }

        // 将顶点数据传输到GPU
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // 设置顶点属性指针
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // 解绑VBO和VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public static void main(String[] args) {
        new GridRenderer().run();
    }
}