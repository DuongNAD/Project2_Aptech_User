package org.example.service;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class CodeCompiler {

    // --- HÀM ĐIỀU PHỐI CHÍNH ---
    public String runCode(String userCode, String language) {
        if (language == null || language.isEmpty()) language = "java";

        switch (language.toLowerCase()) {
            case "cpp":
            case "c++":
            case "c": 
                return runCppCode(userCode);
            case "nodejs":
            case "javascript":
            case "js":
                return runNodeJsCode(userCode);
            case "java":
            default:
                return runJavaCode(userCode);
        }
    }

    // --- 1. XỬ LÝ JAVA (ĐÃ PHỤC HỒI CODE) ---
    private String runJavaCode(String userCode) {
        StringBuilder output = new StringBuilder();
        File sourceFile = new File("Main.java");
        File classFile = new File("Main.class");

        try {
            // B1: Ghi file
            FileWriter writer = new FileWriter(sourceFile);
            writer.write(userCode);
            writer.close();

            // B2: Biên dịch (javac)
            ProcessBuilder compileBuilder = new ProcessBuilder("javac", "Main.java");
            compileBuilder.redirectErrorStream(true);
            Process compileProcess = compileBuilder.start();

            String compileError = readStream(compileProcess.getInputStream());
            if (!compileProcess.waitFor(10, TimeUnit.SECONDS) || compileProcess.exitValue() != 0) {
                return "❌ LỖI BIÊN DỊCH JAVA:\n" + compileError;
            }

            // B3: Chạy (java)
            ProcessBuilder runBuilder = new ProcessBuilder("java", "Main");
            runBuilder.redirectErrorStream(true);
            Process runProcess = runBuilder.start();

            String runOutput = readStream(runProcess.getInputStream());
            if (runProcess.waitFor(5, TimeUnit.SECONDS)) {
                output.append(runOutput);
            } else {
                runProcess.destroy();
                output.append("⚠️ Time Limit Exceeded.");
            }

        } catch (Exception e) {
            return "Lỗi hệ thống Java: " + e.getMessage();
        } finally {
            if (sourceFile.exists()) sourceFile.delete();
            if (classFile.exists()) classFile.delete();
        }
        return output.toString().trim();
    }

    private String runCppCode(String userCode) {
        StringBuilder output = new StringBuilder();
        File sourceFile = new File("Solution.cpp");
        String exeName = System.getProperty("os.name").toLowerCase().contains("win") ? "Solution.exe" : "./Solution";
        File exeFile = new File(exeName);

        try {
            // B1: Ghi file
            FileWriter writer = new FileWriter(sourceFile);
            writer.write(userCode);
            writer.close();

            // B2: Biên dịch (g++)
            // LƯU Ý: Máy anh phải cài MinGW và gõ được lệnh 'g++' trong CMD thì mới chạy được nhé
            ProcessBuilder compileBuilder = new ProcessBuilder("g++", "Solution.cpp", "-o", "Solution");
            compileBuilder.redirectErrorStream(true);
            Process compileProcess = compileBuilder.start();

            String compileError = readStream(compileProcess.getInputStream());
            // C++ biên dịch lâu hơn chút, chờ 10s
            if (!compileProcess.waitFor(10, TimeUnit.SECONDS) || compileProcess.exitValue() != 0) {
                return "❌ LỖI BIÊN DỊCH C/C++:\n" + compileError;
            }

            // B3: Chạy file .exe
            ProcessBuilder runBuilder = new ProcessBuilder(exeFile.getAbsolutePath());
            runBuilder.redirectErrorStream(true);
            Process runProcess = runBuilder.start();

            String runOutput = readStream(runProcess.getInputStream());
            if (runProcess.waitFor(5, TimeUnit.SECONDS)) {
                output.append(runOutput);
            } else {
                runProcess.destroy();
                output.append("⚠️ Time Limit Exceeded.");
            }

        } catch (Exception e) {
            return "Lỗi hệ thống C++ (Kiểm tra xem đã cài MinGW chưa): " + e.getMessage();
        } finally {
            if (sourceFile.exists()) sourceFile.delete();
            if (exeFile.exists()) exeFile.delete();
        }
        return output.toString().trim();
    }

    // --- 3. XỬ LÝ NODE.JS ---
    private String runNodeJsCode(String userCode) {
        StringBuilder output = new StringBuilder();
        File sourceFile = new File("script.js");
        try {
            FileWriter writer = new FileWriter(sourceFile);
            writer.write(userCode);
            writer.close();

            ProcessBuilder runBuilder = new ProcessBuilder("node", "script.js");
            runBuilder.redirectErrorStream(true);
            Process runProcess = runBuilder.start();

            String runOutput = readStream(runProcess.getInputStream());
            if (runProcess.waitFor(5, TimeUnit.SECONDS)) output.append(runOutput);
            else { runProcess.destroy(); output.append("⚠️ Timeout."); }
        } catch (Exception e) { return "Lỗi Node.js: " + e.getMessage(); }
        finally { if (sourceFile.exists()) sourceFile.delete(); }
        return output.toString().trim();
    }

    private String readStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}