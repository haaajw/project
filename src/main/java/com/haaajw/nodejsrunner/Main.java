package com.haaajw.nodejsrunner;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Main extends JavaPlugin {
    
    private Process nodeProcess;

    @Override
    public void onEnable() {
        getLogger().info("Mempersiapkan Node.js Runner...");

        // Jalankan di thread terpisah agar server Minecraft tidak freeze
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                // Path ke folder node.js di server
                String nodePath = "./node/bin/node";
                String npmPath = "./node/bin/npm";

                File node = new File(nodePath);
                File npm = new File(npmPath);

                if (!node.exists() || !npm.exists()) {
                    getLogger().severe("ERROR: Node.js atau npm tidak ditemukan!");
                    getLogger().severe("Pastikan folder 'node' ada di folder utama server.");
                    return;
                }

                // --- 1. JALANKAN NPM INSTALL ---
                getLogger().info("Menjalankan npm install...");
                ProcessBuilder npmPb = new ProcessBuilder(npm.getAbsolutePath(), "install");
                npmPb.directory(new File(".")); // Jalan di root folder server
                npmPb.inheritIO();
                
                Process npmProcess = npmPb.start();
                int npmExitCode = npmProcess.waitFor();

                if (npmExitCode != 0) {
                    getLogger().severe("ERROR: npm install gagal! Exit code: " + npmExitCode);
                    return;
                }
                getLogger().info("npm install sukses! Melanjutkan ke index.js...");

                // --- 2. JALANKAN INDEX.JS ---
                ProcessBuilder pb = new ProcessBuilder(node.getAbsolutePath(), "index.js");
                pb.directory(new File("."));
                pb.inheritIO();

                nodeProcess = pb.start();
                int exitCode = nodeProcess.waitFor();

                getLogger().info("Node.js berhenti dengan exit code: " + exitCode);

            } catch (Exception e) {
                getLogger().severe("Terjadi kesalahan saat menjalankan Node.js!");
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onDisable() {
        if (nodeProcess != null && nodeProcess.isAlive()) {
            getLogger().info("Server mati, menghentikan proses Node.js...");
            nodeProcess.destroy(); // Matikan nodejs supaya tidak nyangkut
        }
    }
}
