package com.ossobo.nexusfx.AlertSystem.sound;

import com.ossobo.nexusfx.AlertSystem.model.TipoAlerta;
import com.ossobo.nexusfx.AlertSystem.model.TipoConfirmacao;
import com.ossobo.nexusfx.resources.api.ResourceAPI;
import com.ossobo.nexusfx.resources.enums.ResourceType;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 🎵 Sistema de sons dos alertas.
 * Integrado ao ResourceAPI para busca de sons registrados.
 *
 * v2.2 (24/04/2026):
 * - ✅ tocarSomUrl() recebe URL direta do AlertDescriptor
 * - ✅ REMOVIDO: carregarPorPathFallback() e paths hardcoded
 * - ✅ REMOVIDO: carregarSom() com múltiplas tentativas de path
 * - ✅ Inicialização exclusivamente via ResourceAPI
 * - ✅ Fallback de path removido — tudo via ResourceAPI
 */
public class AlertaSons {
    private static final Map<String, MediaPlayer> mediaPlayers = new HashMap<>();
    private static final Map<String, AudioClip> audioClips = new HashMap<>();

    private static ResourceAPI resourceAPI;

    /** Mapeamento de nomes internos de som → IDs no ResourceAPI */
    private static final Map<String, String> SOM_PARA_RESOURCE_ID = new HashMap<>();

    static {
        SOM_PARA_RESOURCE_ID.put("info", "fx-sound-info");
        SOM_PARA_RESOURCE_ID.put("warn", "fx-sound-warning");
        SOM_PARA_RESOURCE_ID.put("erro", "fx-sound-error");
        SOM_PARA_RESOURCE_ID.put("critical", "fx-sound-critical");
        SOM_PARA_RESOURCE_ID.put("confirmation", "fx-sound-confirmation");
        SOM_PARA_RESOURCE_ID.put("confirmacao_padrao", "fx-sound-confirmation");
        SOM_PARA_RESOURCE_ID.put("confirmacao_perigo", "fx-sound-warning");
        SOM_PARA_RESOURCE_ID.put("confirmacao_aviso", "fx-sound-warning");
        SOM_PARA_RESOURCE_ID.put("confirmacao_info", "fx-sound-info");
        SOM_PARA_RESOURCE_ID.put("confirmacao_sucesso", "fx-sound-info");
    }

    private static double volumeGeral = 0.7;
    private static boolean inicializado = false;

    private AlertaSons() {
        // Classe utilitária
    }

    // ==================== INTEGRAÇÃO COM RESOURCE API ====================

    /**
     * Víncula o ResourceAPI ao sistema de sons.
     * Chamado pelo AlertaSystem durante o bootstrap.
     */
    public static void setResourceAPI(ResourceAPI api) {
        resourceAPI = api;
        System.out.println("✅ ResourceAPI vinculado ao AlertaSons");
    }

    /**
     * Obtém URL do som via ResourceAPI pelo nome interno.
     * Retorna null se não encontrado.
     */
    private static URL obterUrlDoResource(String nomeSom) {
        if (resourceAPI == null) return null;

        String resourceId = SOM_PARA_RESOURCE_ID.get(nomeSom);
        if (resourceId == null) return null;

        try {
            if (resourceAPI.exists(resourceId, ResourceType.SOUND)) {
                return resourceAPI.getSoundUrl(resourceId);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao buscar som '" + resourceId + "': " + e.getMessage());
        }

        return null;
    }

    // ==================== INICIALIZAÇÃO ====================

    /**
     * Inicializa o sistema de sons carregando todos os sons do ResourceAPI.
     * Seguro para chamadas múltiplas (idempotente).
     */
    public static synchronized void inicializar() {
        if (inicializado) return;

        try {
            carregarSomPorNome("info");
            carregarSomPorNome("warn");
            carregarSomPorNome("erro");
            carregarSomPorNome("critical");
            carregarSomPorNome("confirmation");

            inicializado = true;
            System.out.println("🔊 AlertaSons inicializado. Sons carregados: " +
                    (mediaPlayers.size() + audioClips.size()));

        } catch (Exception e) {
            System.err.println("❌ Erro ao inicializar AlertaSons: " + e.getMessage());
        }
    }

    /**
     * Carrega um som pelo nome interno via ResourceAPI.
     */
    private static void carregarSomPorNome(String nome) {
        URL url = obterUrlDoResource(nome);
        if (url != null) {
            registrarSom(nome, url);
        } else {
            System.err.println("⚠️ Som não encontrado no ResourceAPI: " + nome);
        }
    }

    /**
     * Registra um som carregado no cache interno.
     */
    private static void registrarSom(String nome, URL url) {
        try {
            String urlString = url.toExternalForm();

            if (urlString.toLowerCase().endsWith(".mp3")) {
                Media media = new Media(urlString);
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(volumeGeral);
                player.setCycleCount(1);
                mediaPlayers.put(nome, player);
            } else {
                AudioClip clip = new AudioClip(urlString);
                clip.setVolume(volumeGeral);
                clip.setCycleCount(1);
                audioClips.put(nome, clip);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Falha ao registrar som '" + nome + "': " + e.getMessage());
        }
    }

    // ==================== TOCAR SONS ====================

    /**
     * ✅ Toca som a partir de uma URL direta.
     * Usado pelo AlertaSystem quando o AlertDescriptor já fornece a URL.
     */
    public static void tocarSomUrl(URL soundUrl) {
        if (soundUrl == null) return;
        if (!inicializado) inicializar();

        javafx.application.Platform.runLater(() -> {
            try {
                String urlString = soundUrl.toExternalForm();
                if (urlString.toLowerCase().endsWith(".mp3")) {
                    Media media = new Media(urlString);
                    MediaPlayer player = new MediaPlayer(media);
                    player.setVolume(volumeGeral);
                    player.setCycleCount(1);
                    player.setOnEndOfMedia(() -> player.dispose());
                    player.play();
                } else {
                    AudioClip clip = new AudioClip(urlString);
                    clip.setVolume(volumeGeral);
                    clip.setCycleCount(1);
                    clip.play();
                }
            } catch (Exception e) {
                System.err.println("⚠️ Falha ao tocar som: " + e.getMessage());
            }
        });
    }

    /**
     * Toca som baseado no tipo de alerta.
     * Usado como fallback quando o AlertDescriptor não tem som.
     */
    public static void tocarSom(TipoAlerta tipo) {
        if (!inicializado) inicializar();

        String nomeSom = tipo.name().toLowerCase();
        tocarSomPorNome(nomeSom);
    }

    /**
     * Toca som de confirmação baseado no tipo.
     * Usado como fallback quando o AlertDescriptor não tem som.
     */
    public static void tocarSomConfirmacao(TipoConfirmacao tipo) {
        if (!inicializado) inicializar();

        String nomeSom = "confirmacao_" + tipo.name().toLowerCase();

        if (!somDisponivel(nomeSom)) {
            nomeSom = "confirmation";
        }

        tocarSomPorNome(nomeSom);
    }

    /**
     * Toca um som pelo nome interno.
     */
    private static void tocarSomPorNome(String nomeSom) {
        javafx.application.Platform.runLater(() -> {
            // Tentar MediaPlayer (MP3)
            MediaPlayer player = mediaPlayers.get(nomeSom);
            if (player != null) {
                if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                    player.stop();
                }
                player.seek(javafx.util.Duration.ZERO);
                player.setVolume(volumeGeral);
                player.play();
                return;
            }

            // Tentar AudioClip
            AudioClip clip = audioClips.get(nomeSom);
            if (clip != null) {
                clip.setVolume(volumeGeral);
                clip.play();
                return;
            }

            // Tentar carregar sob demanda via ResourceAPI
            URL url = obterUrlDoResource(nomeSom);
            if (url != null) {
                registrarSom(nomeSom, url);
                tocarSomPorNome(nomeSom);
                return;
            }

            // Fallback genérico para confirmações não mapeadas
            if (nomeSom.startsWith("confirmacao_")) {
                tocarSomPorNome("confirmation");
            }
        });
    }

    /**
     * Toca um som personalizado por ID do ResourceAPI ou caminho.
     */
    public static void tocarSomPersonalizado(String caminhoOuId) {
        if (!inicializado) inicializar();

        javafx.application.Platform.runLater(() -> {
            try {
                URL url = null;

                // Tentar como ID do ResourceAPI
                if (resourceAPI != null && resourceAPI.exists(caminhoOuId, ResourceType.SOUND)) {
                    url = resourceAPI.getSoundUrl(caminhoOuId);
                }

                if (url != null) {
                    tocarSomUrl(url);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Falha ao tocar som personalizado: " + e.getMessage());
            }
        });
    }

    // ==================== CONTROLE DE VOLUME ====================

    /**
     * Define o volume geral (0.0 a 1.0).
     */
    public static void setVolumeGeral(double volume) {
        volumeGeral = Math.max(0.0, Math.min(1.0, volume));

        mediaPlayers.values().forEach(player ->
                player.setVolume(volumeGeral)
        );

        audioClips.values().forEach(clip ->
                clip.setVolume(volumeGeral)
        );
    }

    // ==================== PARAR SONS ====================

    /**
     * Para todos os sons em execução.
     */
    public static void pararTodos() {
        mediaPlayers.values().forEach(player -> {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.stop();
            }
        });
    }

    /**
     * Para um som específico pelo nome.
     */
    public static void pararSom(String nome) {
        MediaPlayer player = mediaPlayers.get(nome);
        if (player != null && player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.stop();
        }
    }

    // ==================== CONSULTA ====================

    /**
     * Verifica se um som está disponível (cache ou ResourceAPI).
     */
    public static boolean somDisponivel(String nome) {
        if (mediaPlayers.containsKey(nome) || audioClips.containsKey(nome)) {
            return true;
        }

        String resourceId = SOM_PARA_RESOURCE_ID.get(nome);
        return resourceAPI != null && resourceId != null &&
                resourceAPI.exists(resourceId, ResourceType.SOUND);
    }

    // ==================== GERENCIAMENTO DE MEMÓRIA ====================

    /**
     * Remove um som do cache.
     */
    public static void liberarSom(String nome) {
        MediaPlayer player = mediaPlayers.remove(nome);
        if (player != null) {
            player.dispose();
        }
        audioClips.remove(nome);
    }

    /**
     * Reinicializa completamente o sistema de sons.
     */
    public static void reinicializar() {
        pararTodos();
        mediaPlayers.values().forEach(MediaPlayer::dispose);
        mediaPlayers.clear();
        audioClips.clear();
        inicializado = false;
        inicializar();
    }

    // ==================== DIAGNÓSTICO ====================

    /**
     * Diagnóstico completo do sistema de sons.
     */
    public static void diagnosticar() {
        System.out.println("\n🔊 ALERTA SONS - DIAGNÓSTICO");
        System.out.println("=".repeat(50));
        System.out.println("• ResourceAPI: " + (resourceAPI != null ? "✅ Vinculado" : "❌ Não vinculado"));
        System.out.println("• Inicializado: " + (inicializado ? "✅ Sim" : "❌ Não"));
        System.out.println("• Volume: " + (volumeGeral * 100) + "%");
        System.out.println("• MediaPlayers em cache: " + mediaPlayers.size());
        System.out.println("• AudioClips em cache: " + audioClips.size());

        System.out.println("\n📋 SONS EM CACHE:");
        if (mediaPlayers.isEmpty() && audioClips.isEmpty()) {
            System.out.println("  (nenhum)");
        } else {
            mediaPlayers.keySet().forEach(nome ->
                    System.out.println("  • " + nome + " [MediaPlayer]"));
            audioClips.keySet().forEach(nome ->
                    System.out.println("  • " + nome + " [AudioClip]"));
        }

        if (resourceAPI != null) {
            System.out.println("\n📋 SONS NO RESOURCE API:");
            resourceAPI.listIdsByType(ResourceType.SOUND).forEach(id ->
                    System.out.println("  • " + id + " → " +
                            (resourceAPI.exists(id) ? "✅ Disponível" : "❌ Indisponível"))
            );
        }

        System.out.println("=".repeat(50));
    }
}