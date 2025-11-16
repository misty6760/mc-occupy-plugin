package org.think_ing.occupyplugin.tpa;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

/**
 * TPA 명령어 실행자
 * /tpa, /tpaaccept, /tpadeny 명령어를 처리합니다
 */
public class TPACommandExecutor implements CommandExecutor {

   private final TPAManager tpaManager;
   private final TeamValidator teamValidator;

   public TPACommandExecutor(TPAManager tpaManager, TeamValidator teamValidator) {
      this.tpaManager = tpaManager;
      this.teamValidator = teamValidator;
   }

   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
         @NotNull String label, @NotNull String[] args) {

      if (!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다.");
         return true;
      }

      Player player = (Player) sender;
      String commandName = command.getName().toLowerCase();

      switch (commandName) {
         case "tpa":
            return handleTPA(player, args);
         case "tpaaccept":
            return handleTPAAccept(player);
         case "tpadeny":
            return handleTPADeny(player);
         default:
            return false;
      }
   }

   /**
    * /tpa 명령어 처리
    */
   private boolean handleTPA(Player player, String[] args) {
      // 사용법 확인
      if (args.length != 1) {
         player.sendMessage(ChatColor.RED + "사용법: /tpa <플레이어>");
         return true;
      }

      // 대상 플레이어 조회
      Player target = Bukkit.getPlayer(args[0]);
      if (target == null) {
         player.sendMessage(ChatColor.RED + "플레이어 '" + args[0] + "'을(를) 찾을 수 없습니다.");
         return true;
      }

      // 자기 자신에게 요청 방지
      if (target.equals(player)) {
         player.sendMessage(ChatColor.RED + "자기 자신에게는 텔레포트 요청을 보낼 수 없습니다.");
         return true;
      }

      // 팀 확인
      if (!teamValidator.hasTeam(player)) {
         player.sendMessage(ChatColor.RED + "팀에 속해있지 않습니다. /team 명령어로 팀에 가입해주세요.");
         return true;
      }

      if (!teamValidator.hasTeam(target)) {
         player.sendMessage(ChatColor.RED + target.getName() + "님은 팀에 속해있지 않습니다.");
         return true;
      }

      if (!teamValidator.areSameTeam(player, target)) {
         Team playerTeam = teamValidator.getPlayerTeam(player);
         Team targetTeam = teamValidator.getPlayerTeam(target);

         @SuppressWarnings("deprecation")
         String playerTeamName = playerTeam != null ? playerTeam.getDisplayName() : "없음";
         @SuppressWarnings("deprecation")
         String targetTeamName = targetTeam != null ? targetTeam.getDisplayName() : "없음";

         player.sendMessage(ChatColor.RED + "같은 팀원에게만 텔레포트 요청을 보낼 수 있습니다.");
         player.sendMessage(ChatColor.GRAY + "당신의 팀: " + playerTeamName + " | " +
               target.getName() + "의 팀: " + targetTeamName);
         return true;
      }

      // 쿨다운 확인
      if (tpaManager.isOnCooldown(player)) {
         int remaining = tpaManager.getRemainingCooldown(player);
         String cooldownType = tpaManager.getCooldownType(player);
         if ("teleport".equals(cooldownType)) {
            int minutes = remaining / 60;
            int seconds = remaining % 60;
            if (minutes > 0) {
               player.sendMessage(ChatColor.RED + "텔레포트 쿨다운 중입니다. (" + minutes + "분 " + seconds + "초 남음)");
            } else {
               player.sendMessage(ChatColor.RED + "텔레포트 쿨다운 중입니다. (" + seconds + "초 남음)");
            }
         } else {
            player.sendMessage(ChatColor.RED + "텔레포트 요청 쿨다운 중입니다. (" + remaining + "초 남음)");
         }
         return true;
      }

      // 요청 생성
      tpaManager.createRequest(player, target);

      int expirationSeconds = tpaManager.getExpirationSeconds();

      player.sendMessage(ChatColor.GREEN + target.getName() + "님에게 텔레포트 요청을 보냈습니다.");
      player.sendMessage(ChatColor.GRAY + "요청은 " + expirationSeconds + "초 후에 만료됩니다.");

      target.sendMessage(ChatColor.YELLOW + "━━━━━━━━━━━━━━━━━━━━━━━━━");
      target.sendMessage(ChatColor.GREEN + player.getName() + "님이 텔레포트 요청을 보냈습니다!");
      target.sendMessage(ChatColor.WHITE + "수락: " + ChatColor.GREEN + "/tpaaccept");
      target.sendMessage(ChatColor.WHITE + "거부: " + ChatColor.RED + "/tpadeny");
      target.sendMessage(ChatColor.GRAY + "요청은 " + expirationSeconds + "초 후에 만료됩니다.");
      target.sendMessage(ChatColor.YELLOW + "━━━━━━━━━━━━━━━━━━━━━━━━━");

      return true;
   }

   /**
    * /tpaaccept 명령어 처리
    */
   private boolean handleTPAAccept(Player player) {
      TPARequest request = tpaManager.getPendingRequest(player);

      if (request == null) {
         player.sendMessage(ChatColor.RED + "대기 중인 텔레포트 요청이 없습니다.");
         return true;
      }

      Player requester = request.getRequester();
      if (!requester.isOnline()) {
         player.sendMessage(ChatColor.RED + "요청자가 오프라인 상태입니다.");
         tpaManager.denyRequest(player);
         return true;
      }

      // 요청 수락 및 텔레포트
      if (tpaManager.acceptRequest(player)) {
         player.sendMessage(ChatColor.GREEN + requester.getName() + "님의 텔레포트 요청을 수락했습니다!");
      } else {
         player.sendMessage(ChatColor.RED + "텔레포트 요청 수락에 실패했습니다.");
      }

      return true;
   }

   /**
    * /tpadeny 명령어 처리
    */
   private boolean handleTPADeny(Player player) {
      TPARequest request = tpaManager.getPendingRequest(player);

      if (request == null) {
         player.sendMessage(ChatColor.RED + "대기 중인 텔레포트 요청이 없습니다.");
         return true;
      }

      Player requester = request.getRequester();

      if (tpaManager.denyRequest(player)) {
         player.sendMessage(ChatColor.YELLOW + requester.getName() + "님의 텔레포트 요청을 거부했습니다.");

         if (requester.isOnline()) {
            requester.sendMessage(ChatColor.RED + player.getName() + "님이 텔레포트 요청을 거부했습니다.");
         }
      } else {
         player.sendMessage(ChatColor.RED + "텔레포트 요청 거부에 실패했습니다.");
      }

      return true;
   }
}
