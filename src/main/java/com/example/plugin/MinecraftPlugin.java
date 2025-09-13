package com.example.plugin;

import com.example.plugin.capture.CaptureManager;
import com.example.plugin.effects.ZoneEffectManager;
import com.example.plugin.exchange.ExchangeManager;
import com.example.plugin.team.TeamManager;
import com.example.plugin.tpa.TPAManager;
import com.example.plugin.beacon.BeaconManager;
import com.example.plugin.commands.*;
import com.example.plugin.listeners.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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
    
    // 플러그인 시작 시간
    private long pluginStartTime;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        this.pluginStartTime = startTime;
        
        getLogger().info(ChatColor.GOLD + "==========================================");
        getLogger().info(ChatColor.GREEN + "🚀 땅따먹기 플러그인 활성화 시작...");
        getLogger().info(ChatColor.YELLOW + "📦 버전: " + getDescription().getVersion());
        getLogger().info(ChatColor.GOLD + "==========================================");
        
        try {
            instance = this;
            getLogger().info(ChatColor.AQUA + "✅ 플러그인 인스턴스 초기화 완료");
            
            // 매니저들 초기화
            initializeManagers();
            getLogger().info(ChatColor.AQUA + "✅ 매니저 시스템 초기화 완료");
            
            // 명령어 등록
            registerCommands();
            getLogger().info(ChatColor.AQUA + "✅ 명령어 시스템 등록 완료");
            
            // 이벤트 리스너 등록
            registerEventListeners();
            getLogger().info(ChatColor.AQUA + "✅ 이벤트 리스너 등록 완료");
            
            // 설정 파일 로드
            saveDefaultConfig();
            getLogger().info(ChatColor.AQUA + "✅ 설정 파일 초기화 완료");
            
            long endTime = System.currentTimeMillis();
            long loadTime = endTime - startTime;
            
            getLogger().info(ChatColor.GOLD + "==========================================");
            getLogger().info(ChatColor.GREEN + "🎉 땅따먹기 플러그인 활성화 완료!");
            getLogger().info(ChatColor.YELLOW + "⏱️ 로드 시간: " + loadTime + "ms");
            getLogger().info(ChatColor.AQUA + "📝 등록된 명령어: 12개");
            getLogger().info(ChatColor.AQUA + "🗺️ 점령지: " + captureManager.getAllCaptureZones().size() + "개");
            getLogger().info(ChatColor.AQUA + "👥 최대 팀 수: 4개");
            getLogger().info(ChatColor.GOLD + "==========================================");
            
        } catch (Exception e) {
            getLogger().severe("플러그인 활성화 중 오류 발생!");
            getLogger().severe("오류 내용: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        long disableStartTime = System.currentTimeMillis();
        
        getLogger().info(ChatColor.GOLD + "==========================================");
        getLogger().info(ChatColor.RED + "🛑 땅따먹기 플러그인 비활성화 시작...");
        getLogger().info(ChatColor.YELLOW + "📦 버전: " + getDescription().getVersion());
        getLogger().info(ChatColor.GOLD + "==========================================");
        
        try {
            // 게임 중단
            if (captureManager != null) {
                captureManager.stopGame();
                getLogger().info(ChatColor.AQUA + "✅ 진행 중인 게임 중단 완료");
            }
            
            // 모든 효과 중단
            if (effectManager != null) {
                effectManager.stopAllEffects();
                getLogger().info(ChatColor.AQUA + "✅ 모든 구역 효과 중단 완료");
            }
            
            // TPA 요청 정리
            if (tpaManager != null) {
                tpaManager.clearAllRequests();
                getLogger().info(ChatColor.AQUA + "✅ 모든 TPA 요청 정리 완료");
            }
            
            // 신호기 색상 초기화
            if (beaconManager != null) {
                beaconManager.resetAllBeaconColors();
                getLogger().info(ChatColor.AQUA + "✅ 모든 신호기 색상 초기화 완료");
            }
            
            // 매니저 정리
            teamManager = null;
            captureManager = null;
            effectManager = null;
            exchangeManager = null;
            tpaManager = null;
            beaconManager = null;
            getLogger().info(ChatColor.AQUA + "✅ 모든 매니저 정리 완료");
            
            long disableEndTime = System.currentTimeMillis();
            long disableTime = disableEndTime - disableStartTime;
            
            getLogger().info(ChatColor.GOLD + "==========================================");
            getLogger().info(ChatColor.RED + "🎉 땅따먹기 플러그인 비활성화 완료!");
            getLogger().info(ChatColor.YELLOW + "⏱️ 비활성화 시간: " + disableTime + "ms");
            getLogger().info(ChatColor.AQUA + "📊 플러그인 통계:");
            
            // 총 실행 시간 계산
            long totalRunTime = System.currentTimeMillis() - pluginStartTime;
            long totalRunTimeSeconds = totalRunTime / 1000;
            long totalRunTimeMinutes = totalRunTimeSeconds / 60;
            long remainingSeconds = totalRunTimeSeconds % 60;
            
            String runTimeText;
            if (totalRunTimeMinutes > 0) {
                runTimeText = totalRunTimeMinutes + "분 " + remainingSeconds + "초";
            } else {
                runTimeText = totalRunTimeSeconds + "초";
            }
            
            getLogger().info(ChatColor.GRAY + "  - 총 실행 시간: " + runTimeText);
            getLogger().info(ChatColor.GRAY + "  - 메모리 사용량: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + "MB");
            getLogger().info(ChatColor.GRAY + "  - 정리된 매니저: 6개");
            getLogger().info(ChatColor.GRAY + "  - 정리된 명령어: 12개");
            getLogger().info(ChatColor.GOLD + "==========================================");
            
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
        
        LeaveCommand leaveCommand = new LeaveCommand(teamManager);
        getCommand("leave").setExecutor(leaveCommand);
        getCommand("leave").setTabCompleter(leaveCommand);
        
        // 게임 관련 명령어
        GameCommand gameCommand = new GameCommand(captureManager, teamManager);
        getCommand("game").setExecutor(gameCommand);
        getCommand("game").setTabCompleter(gameCommand);
        
        CaptureCommand captureCommand = new CaptureCommand(captureManager);
        getCommand("capture").setExecutor(captureCommand);
        getCommand("capture").setTabCompleter(captureCommand);
        
        // 교환 관련 명령어
        ExchangeCommand exchangeCommand = new ExchangeCommand(exchangeManager);
        getCommand("exchange").setExecutor(exchangeCommand);
        getCommand("exchange").setTabCompleter(exchangeCommand);
        
        // TPA 관련 명령어
        TPACommand tpaCommand = new TPACommand(tpaManager);
        getCommand("tpa").setExecutor(tpaCommand);
        getCommand("tpa").setTabCompleter(tpaCommand);
        getCommand("tpaccept").setExecutor(tpaCommand);
        getCommand("tpaccept").setTabCompleter(tpaCommand);
        getCommand("tpdeny").setExecutor(tpaCommand);
        getCommand("tpdeny").setTabCompleter(tpaCommand);
        getCommand("tpcancel").setExecutor(tpaCommand);
        getCommand("tpcancel").setTabCompleter(tpaCommand);
        getCommand("tpastatus").setExecutor(tpaCommand);
        getCommand("tpastatus").setTabCompleter(tpaCommand);
        
        // 점령지 설정 명령어
        File zonesFile = new File(getDataFolder(), "zones.yml");
        ZoneCommand zoneCommand = new ZoneCommand(captureManager, beaconManager, zonesFile);
        getCommand("zone").setExecutor(zoneCommand);
        getCommand("zone").setTabCompleter(zoneCommand);
        
        // 정보 명령어
        InfoCommand infoCommand = new InfoCommand(teamManager, captureManager);
        getCommand("info").setExecutor(infoCommand);
        getCommand("info").setTabCompleter(infoCommand);
        
        // 테스트 명령어
        TestCommand testCommand = new TestCommand(captureManager);
        getCommand("test").setExecutor(testCommand);
        getCommand("test").setTabCompleter(testCommand);
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
