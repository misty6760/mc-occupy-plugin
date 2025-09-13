package com.example.plugin.team;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 팀 클래스
 * 3-4팀, 각 팀당 3명으로 구성
 */
public class Team {
    private String name;
    private final ChatColor color;
    private final List<UUID> members;
    private final int maxMembers = 3;
    private int score = 0;

    public Team(String name, ChatColor color) {
        this.name = name;
        this.color = color;
        this.members = new ArrayList<>();
    }

    /**
     * 플레이어를 팀에 추가
     * @param player 추가할 플레이어
     * @return 추가 성공 여부
     */
    public boolean addMember(Player player) {
        if (members.size() >= maxMembers) {
            return false;
        }
        if (members.contains(player.getUniqueId())) {
            return false;
        }
        members.add(player.getUniqueId());
        return true;
    }

    /**
     * 플레이어를 팀에서 제거
     * @param player 제거할 플레이어
     * @return 제거 성공 여부
     */
    public boolean removeMember(Player player) {
        return members.remove(player.getUniqueId());
    }

    /**
     * 팀 이름 설정
     * @param name 새로운 팀 이름
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 팀에 플레이어가 있는지 확인
     * @param player 확인할 플레이어
     * @return 팀원 여부
     */
    public boolean hasMember(Player player) {
        return members.contains(player.getUniqueId());
    }

    /**
     * 팀원 수 반환
     * @return 팀원 수
     */
    public int getMemberCount() {
        return members.size();
    }

    /**
     * 팀이 가득 찼는지 확인
     * @return 가득 참 여부
     */
    public boolean isFull() {
        return members.size() >= maxMembers;
    }

    /**
     * 팀이 비어있는지 확인
     * @return 비어있음 여부
     */
    public boolean isEmpty() {
        return members.isEmpty();
    }

    /**
     * 점수 추가
     * @param points 추가할 점수
     */
    public void addScore(int points) {
        this.score += points;
    }

    /**
     * 점수 설정
     * @param score 설정할 점수
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * 현재 점수 반환
     * @return 현재 점수
     */
    public int getScore() {
        return score;
    }

    /**
     * 팀 이름 반환
     * @return 팀 이름
     */
    public String getName() {
        return name;
    }

    /**
     * 팀 색상 반환
     * @return 팀 색상
     */
    public ChatColor getColor() {
        return color;
    }

    /**
     * 팀원 UUID 목록 반환
     * @return 팀원 UUID 목록
     */
    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    /**
     * 팀 정보를 문자열로 반환
     * @return 팀 정보 문자열
     */
    @Override
    public String toString() {
        return color + name + ChatColor.WHITE + " (점수: " + score + ", 팀원: " + members.size() + "/" + maxMembers + ")";
    }
}
