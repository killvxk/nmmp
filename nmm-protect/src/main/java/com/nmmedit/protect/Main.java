package com.nmmedit.protect;

import com.nmmedit.apkprotect.ApkFolders;
import com.nmmedit.apkprotect.ApkProtect;
import com.nmmedit.apkprotect.deobfus.MappingReader;
import com.nmmedit.apkprotect.dex2c.converter.ClassAnalyzer;
import com.nmmedit.apkprotect.dex2c.converter.instructionrewriter.RandomInstructionRewriter;
import com.nmmedit.apkprotect.dex2c.filters.*;
import com.nmmedit.apkprotect.sign.ApkVerifyCodeGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("No Input apk.");
            System.err.println("<inApk> [<convertRuleFile> [mapping.txt]]");
            System.exit(-1);
        }
        final File apk = new File(args[0]);
        final File outDir = new File(apk.getParentFile(), "build");

        ClassAndMethodFilter filterConfig = new BasicKeepConfig();
        final SimpleRules simpleRules = new SimpleRules();
        if (args.length > 1) {
            simpleRules.parse(new InputStreamReader(new FileInputStream(args[1]), StandardCharsets.UTF_8));
        } else {
            //all classes
            simpleRules.parse(new StringReader("class *"));
        }

        if (args.length > 2) {
            final MappingReader mappingReader = new MappingReader(new File(args[2]));
            filterConfig = new ProguardMappingConfig(filterConfig, mappingReader, simpleRules);
        } else {
            filterConfig = new SimpleConvertConfig(new BasicKeepConfig(), simpleRules);
        }

        final ClassAnalyzer classAnalyzer = new ClassAnalyzer();
        //todo 可能需要加载某项厂商私有的sdk


        final ApkFolders apkFolders = new ApkFolders(apk, outDir);

        //apk签名验证相关，不使用
        final ApkVerifyCodeGenerator apkVerifyCodeGenerator = null;

        final ApkProtect apkProtect = new ApkProtect.Builder(apkFolders)
                .setInstructionRewriter(new RandomInstructionRewriter())
                .setApkVerifyCodeGenerator(apkVerifyCodeGenerator)
                .setFilter(filterConfig)
                .setClassAnalyzer(classAnalyzer)
                .build();
        apkProtect.run();
    }
}
