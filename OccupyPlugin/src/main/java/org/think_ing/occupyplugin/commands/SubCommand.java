package org.think_ing.occupyplugin.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * 서브 명령어 인터페이스
 */
public interface SubCommand {

    /**
     * 명령어 실행
     * 
     * @param sender 명령어 실행자
     * @param args   명령어 인자
     * @return 성공 여부
     */
    boolean execute(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * 명령어 이름
     * 
     * @return 명령어 이름
     */
    String getName();

    /**
     * 명령어 설명
     * 
     * @return 명령어 설명
     */
    String getDescription();

    /**
     * 사용법
     * 
     * @return 사용법 문자열
     */
    String getUsage();

    /**
     * 탭 자동완성 목록
     * 
     * @param sender 명령어 실행자
     * @param args   현재까지의 인자
     * @return 자동완성 후보 목록
     */
    default List<String> getTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
