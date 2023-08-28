package com.demo.www;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 自定义服务器
 */
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080); // 监听8080端口
        System.out.println("服务器启动成功，监听端口：8080");
        while (true) {
            System.out.println("等待客户端连接..."+ System.currentTimeMillis());
            // 等待客户端连接
            Socket socket = serverSocket.accept();
            SocketHandler socketHandler = new SocketHandler(socket);
            System.out.println("connected from " + socket.getRemoteSocketAddress());
            // 启动线程
            new Thread(socketHandler).start();
        }
    }
}

class SocketHandler extends Thread {
    Socket socket;

    public SocketHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream inputStream = this.socket.getInputStream();
            OutputStream outputStream = this.socket.getOutputStream();
            handle(inputStream, outputStream);
        } catch (IOException e) {
            try {
                this.socket.close();
            } catch (IOException ex) {}
            e.printStackTrace();
            System.out.println("服务器异常：" + e.getMessage());
        }
    }

    private void handle(InputStream inputStream, OutputStream outputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        // 读取HTTP请求：
        boolean requestOk = false;
        String first = bufferedReader.readLine();
        System.out.println("first: " + first);
        if (first.startsWith("GET / HTTP/1.")) {
            requestOk = true;
        }
        for (;;) {
            String header = bufferedReader.readLine();
            if (header.isEmpty()) { // 读取到空行时, HTTP Header读取完毕
                break;
            }
            System.out.println(header);
        }
        System.out.println(requestOk ? "Response OK" : "Response Error");
        if (!requestOk) {
            // 发送错误响应:
            bufferedWriter.write("HTTP/1.0 404 Not Found\r\n");
            bufferedWriter.write("Content-Length: 0\r\n");
            bufferedWriter.write("\r\n");
        } else {
            // 发送成功响应:
            String data = "<html><body><h1>Hello, world!</h1></body></html>";
            int length = data.getBytes(StandardCharsets.UTF_8).length;
            bufferedWriter.write("HTTP/1.0 200 OK\r\n");
            bufferedWriter.write("Connection: close\r\n");
            bufferedWriter.write("Content-Type: text/html\r\n");
            bufferedWriter.write("Content-Length: " + length + "\r\n");
            bufferedWriter.write("\r\n"); // 空行标识Header和Body的分隔
            bufferedWriter.write(data);
        }
        bufferedWriter.flush();
    }
}
