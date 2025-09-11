package com.example.plugin;

import com.example.plugin.capture.CaptureManager;
import com.example.plugin.effects.ZoneEffectManager;
import com.example.plugin.exchange.ExchangeManager;
import com.example.plugin.team.TeamManager;
import com.example.plugin.tpa.TPAManager;
import com.example.plugin.beacon.BeaconManager;
import com.example.plugin.commands.*;
import com.example.plugin.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 땅따먹기 게임 플러그인 메인 클래스
 * Paper 1.16.5용 마인크래프트 플러그인
 * Java 16 환경에서 빌드 및 실행
 */
public class MinecraftPlugin extends JavaPlugin {

    private static MinecraftPlugin instance;
    
    // 매니저들
    private TeamManager teamManager;
    private CaptureManager captureManager;
    private ZoneEffectManager effectManager;
    private ExchangeManager exchangeManager;
    private TPAManager tpaManager;
    private BeaconManager beaconManager;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        
        getLogger().info("==========================================");
        getLogger().info("땅따먹기 플러그인 활성화 시작...");
        getLogger().info("==========================================");
        
        try {
            instance = this;
            getLogger().info("플러그인 인스턴스 초기화 완료");
            
            // 매니저들 초기화
            initializeManagers();
            getLogger().info("매니저 시스템 초기화 완료");
            
            // 명령어 등록
            registerCommands();
            getLogger().info("명령어 시스템 등록 완료");
            
            // 이벤트 리스너 등록
            registerEventListeners();
            getLogger().info("이벤트 리스너 등록 완료");
            
            // 설정 파일 로드
            saveDefaultConfig();
            getLogger().info("설정 파일 초기화 완료");
            
            long endTime = System.currentTimeMillis();
            long loadTime = endTime - startTime;
            
            getLogger().info("==========================================");
            getLogger().info("땅따먹기 플러그인 활성화 완료!");
            getLogger().info("로드 시간: " + loadTime + "ms");
            getLogger().info("등록된 명령어: 12개");
            getLogger().info("점령지: 6개 (테스트 맵)");
            getLogger().info("👥 최대 팀 수: 4개");
            getLogger().info("==========================================");
            
        } catch (Exception e) {
            getLogger().severe("플러그인 활성화 중 오류 발생!");
            getLogger().severe("오류 내용: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("==========================================");
        getLogger().info("🛑 땅따먹기 플러그인 비활성화 시작...");
        getLogger().info("==========================================");
        
        try {
            // 게임 중단
            if (captureManager != null) {
                captureManager.stopGame();
                getLogger().info("진행 중인 게임 중단 완료");
            }
            
            // 모든 효과 중단
            if (effectManager != null) {
                effectManager.stopAllEffects();
                getLogger().info("모든 구역 효과 중단 완료");
            }
            
            // TPA 요청 정리
            if (tpaManager != null) {
                tpaManager.clearAllRequests();
                getLogger().info("모든 TPA 요청 정리 완료");
            }
            
            // 신호기 색상 초기화
            if (beaconManager != null) {
                beaconManager.resetAllBeaconColors();
                getLogger().info("모든 신호기 색상 초기화 완료");
            }
            
            getLogger().info("==========================================");
            getLogger().info("땅따먹기 플러그인 비활성화 완료!");
            getLogger().info("📊 플러그인 통계:");
            getLogger().info("  - 총 실행 시간: " + getDescription().getVersion());
            getLogger().info("  - 메모리 사용량: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + "MB");
            getLogger().info("==========================================");
            
        } catch (Exception e) {
            getLogger().severe("플러그인 비활성화 중 오류 발생!");
            getLogger().severe("오류 내용: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 매니저들을 초기화합니다
     */
    private void initializeManagers() {
        teamManager = new TeamManager();
        captureManager = new CaptureManager(this, teamManager);
        effectManager = new ZoneEffectManager(this, teamManager);
        exchangeManager = new ExchangeManager();
        tpaManager = new TPAManager(this, teamManager);
        beaconManager = new BeaconManager(this);
        
        // 매니저들 연결
        captureManager.setEffectManager(effectManager);
        captureManager.setBeaconManager(beaconManager);
    }

    /**
     * 명령어들을 등록합니다
     */
    private void registerCommands() {
        // 팀 관련 명령어
        TeamCommand teamCommand = new TeamCommand(teamManager);
        getCommand("team").setExecutor(teamCommand);
        getCommand("team").setTabCompleter(teamCommand);
        
        JoinCommand joinCommand = new JoinCommand(teamManager);
        getCommand("join").setExecutor(joinCommand);
        getCommand("join").setTabCompleter(joinCommand);
        
        getCommand("leave").setExecutor(new LeaveCommand(teamManager));
        
        // 게임 관련 명령어
        GameCommand gameCommand = new GameCommand(captureManager, teamManager);
        getCommand("game").setExecutor(gameCommand);
        getCommand("game").setTabCompleter(gameCommand);
        
        CaptureCommand captureCommand = new CaptureCommand(captureManager);
        getCommand("capture").setExecutor(captureCommand);
        getCommand("capture").setTabCompleter(captureCommand);
        
        // 교환 관련 명령어
        getCommand("exchange").setExecutor(new ExchangeCommand(exchangeManager));
        
        // TPA 관련 명령어
        TPACommand tpaCommand = new TPACommand(tpaManager);
        getCommand("tpa").setExecutor(tpaCommand);
        getCommand("tpa").setTabCompleter(tpaCommand);
        getCommand("tpaccept").setExecutor(tpaCommand);
        getCommand("tpdeny").setExecutor(tpaCommand);
        getCommand("tpcancel").setExecutor(tpaCommand);
        getCommand("tpastatus").setExecutor(tpaCommand);
        
        // 신호기 관련 명령어
        BeaconCommand beaconCommand = new BeaconCommand(beaconManager, captureManager);
        getCommand("beacon").setExecutor(beaconCommand);
        getCommand("beacon").setTabCompleter(beaconCommand);
        
        // 정보 명령어
        getCommand("info").setExecutor(new InfoCommand(teamManager, captureManager));
    }

    /**
     * 이벤트 리스너들을 등록합니다
     */
    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(teamManager, exchangeManager), this);
    }

    /**
     * 플러그인 인스턴스를 반환합니다
     * @return 플러그인 인스턴스
     */
    public static MinecraftPlugin getInstance() {
        return instance;
    }

    /**
     * 팀 매니저 반환
     * @return 팀 매니저
     */
    public TeamManager getTeamManager() {
        return teamManager;
    }

    /**
     * 점령 매니저 반환
     * @return 점령 매니저
     */
    public CaptureManager getCaptureManager() {
        return captureManager;
    }

    /**
     * 효과 매니저 반환
     * @return 효과 매니저
     */
    public ZoneEffectManager getEffectManager() {
        return effectManager;
    }

    /**
     * 교환 매니저 반환
     * @return 교환 매니저
     */
    public ExchangeManager getExchangeManager() {
        return exchangeManager;
    }

    /**
     * TPA 매니저 반환
     * @return TPA 매니저
     */
    public TPAManager getTPAManager() {
        return tpaManager;
    }

    /**
     * 신호기 매니저 반환
     * @return 신호기 매니저
     */
    public BeaconManager getBeaconManager() {
        return beaconManager;
    }
}
