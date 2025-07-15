package com.codewithmosh;

// 导入 LWJGL 相关类
import org.lwjgl.*;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

// 导入静态方法，简化调用
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    // 窗口句柄（用于操作窗口）
    private long window;

    /**
     * 主运行方法
     */
    public void run() {
        // 打印 LWJGL 版本信息
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        // 初始化 GLFW 并创建窗口
        init();

        // 进入主循环
        loop();

        // 清理窗口相关资源
        glfwFreeCallbacks(window); // 释放窗口回调函数
        glfwDestroyWindow(window); // 销毁窗口

        // 终止 GLFW，释放错误回调
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * 初始化 GLFW 和创建窗口
     */
    private void init() {
        // 设置错误回调函数，用于输出 GLFW 错误信息到标准错误流
        GLFWErrorCallback.createPrint(System.err).set();

        // 初始化 GLFW 库，几乎所有 GLFW 函数都必须在初始化后才能使用
        if (!glfwInit())
            throw new IllegalStateException("无法初始化 GLFW");

        // 配置窗口默认提示（可选）
        glfwDefaultWindowHints();

        // 设置窗口不可见（创建后隐藏）
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        // 设置窗口可调整大小
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // 创建一个 300x300 的窗口，标题为 "Hello World!"，无全屏/共享上下文
        window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("创建 GLFW 窗口失败");

        // 设置按键回调函数：当按下 ESC 键时关闭窗口
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // 触发窗口关闭
        });

        // 使用 MemoryStack 分配本地内存，用于获取窗口尺寸
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // 分配 int 类型缓冲区保存宽度
            IntBuffer pHeight = stack.mallocInt(1); // 分配 int 类型缓冲区保存高度

            // 获取窗口当前尺寸
            glfwGetWindowSize(window, pWidth, pHeight);

            // 获取主显示器的视频模式（分辨率等信息）
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // 将窗口居中显示
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2, // 居中 X 坐标
                    (vidmode.height() - pHeight.get(0)) / 2  // 居中 Y 坐标
            );
        } // 自动释放栈内存

        // 将 OpenGL 上下文设置为当前线程使用的上下文
        glfwMakeContextCurrent(window);

        // 启用垂直同步（防止画面撕裂）
        glfwSwapInterval(1);

        // 显示窗口
        glfwShowWindow(window);
    }

    /**
     * 主循环：处理渲染和事件
     */
    private void loop() {
        // LWJGL 初始化 OpenGL 函数绑定（必须在有上下文的线程中调用）
        GL.createCapabilities();

        // 设置清空屏幕后的颜色（红色）
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        // 当窗口未被关闭时持续渲染
        while (!glfwWindowShouldClose(window)) {
            // 清除颜色缓冲和深度缓冲
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // 交换前后缓冲（双缓冲机制）
            glfwSwapBuffers(window);

            // 处理所有挂起的 GLFW 事件（如按键、鼠标、窗口变化）
            glfwPollEvents();
        }
    }

    /**
     * 程序入口点
     */
    public static void main(String[] args) {
        new Main().run(); // 启动程序
    }
}
