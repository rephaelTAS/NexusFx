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
 * 🎵 ALERTA SONS - ADAPTADO AO RESOURCE API
 *
 * v2.0 (22/04/2026):
 * - ✅ Busca sons do ResourceAPI (datacenter)
 * - ✅ Mantém fallback para paths locais
 * - ✅ Compatível com API existente
 */
public class AlertaSons {
    private static final Map<String, MediaPlayer> mediaPlayers = new HashMap<>();
    private static final Map<String, AudioClip> audioClips = new HashMap<>();

    // ✅ NOVO: ResourceAPI (datacenter)
    private static ResourceAPI resourceAPI;

    // Mapeamento de nomes de som → IDs no ResourceAPI
    private static final Map<String, String> SOM_PARA_RESOURCE_ID = new HashMap<>();

    static {
        // Mapear nomes de som para IDs registrados no ResourceAPI
        SOM_PARA_RESOURCE_ID.put("info", "fx-sound-info");
        SOM_PARA_RESOURCE_ID.put("warn", "fx-sound-warning");
        SOM_PARA_RESOURCE_ID.put("erro", "fx-sound-error");
        SOM_PARA_RESOURCE_ID.put("critical", "fx-sound-critical");
        SOM_PARA_RESOURCE_ID.put("confirmation", "fx-sound-confirmation");
        SOM_PARA_RESOURCE_ID.put("confirmacao_padrao", "fx-sound-confirmation");
        SOM_PARA_RESOURCE_ID.put("confirmacao_perigo", "fx-sound-warning");
        SOM_PARA_RESOURCE_ID.put("confirmacao_aviso", "fx-sound-warning");
        SOM_PARA_RESOURCE_ID.put("confirmacao_info", "fx-sound-info");
        SOM_PARA_RESOURCE_ID.put("confirmacao_sucesso", "fx-sound-info"); // fallback
    }

    private static double volumeGeral = 0.7;
    private static boolean inicializado = false;

    // ==================== INTEGRAÇÃO COM RESOURCE API ====================

    /**
     * ✅ Víncula ResourceAPI ao sistema de sons
     */
    public static void setResourceAPI(ResourceAPI api) {
        resourceAPI = api;
        System.out.println("✅ ResourceAPI vinculado ao AlertaSons");
    }

    /**
     * ✅ Obtém URL do som via ResourceAPI
     */
    private static URL obterUrlDoResource(String nomeSom) {
        if (resourceAPI == null) {
            return null;
        }

        String resourceId = SOM_PARA_RESOURCE_ID.get(nomeSom);
        if (resourceId == null) {
            return null;
        }

        try {
            if (resourceAPI.exists(resourceId, ResourceType.SOUND)) {
                return resourceAPI.getSoundUrl(resourceId);
            }
        } catch (Exception e) {
            // Fallback para paths locais
        }

        return null;
    }

    /** Inicializa os recursos de áudio */
    public static synchronized void inicializar() {
        if (inicializado) return;

        try {
            // Carregar sons via ResourceAPI (prioridade) ou fallback
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

    /** Carrega um som por nome (prioriza ResourceAPI) */
    private static void carregarSomPorNome(String nome) {
        // 1. Tentar ResourceAPI
        URL url = obterUrlDoResource(nome);

        // 2. Fallback: tentar carregar por path
        if (url == null) {
            url = carregarPorPathFallback(nome);
        }

        if (url != null) {
            registrarSom(nome, url);
        }
    }

    /** Fallback para paths antigos */
    private static URL carregarPorPathFallback(String nome) {
        // Mapeamento de paths antigos
        Map<String, String> pathsFallback = new HashMap<>();
        pathsFallback.put("info", "/com/ossobo/nexusfx/assets/sound/info.mp3");
        pathsFallback.put("warn", "/com/ossobo/nexusfx/assets/sound/warning.mp3");
        pathsFallback.put("erro", "/com/ossobo/nexusfx/assets/sound/error.mp3");
        pathsFallback.put("critical", "/com/ossobo/nexusfx/assets/sound/critical.mp3");
        pathsFallback.put("confirmation", "/com/ossobo/nexusfx/assets/sound/confirmation.mp3");

        String path = pathsFallback.get(nome);
        if (path != null) {
            return AlertaSons.class.getResource(path);
        }
        return null;
    }

    /** Registra um som carregado */
    private static void registrarSom(String nome, URL url) {
        try {
            String urlString = url.toString();

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

    /** Carrega um som com múltiplas tentativas (mantido para compatibilidade) */
    private static void carregarSom(String nome, String caminho) {
        // Se já temos o som via ResourceAPI, ignorar
        if (mediaPlayers.containsKey(nome) || audioClips.containsKey(nome)) {
            return;
        }

        try {
            // Tentar ResourceAPI primeiro
            URL url = obterUrlDoResource(nome);

            // Fallback para caminho passado
            if (url == null) {
                url = AlertaSons.class.getResource(caminho);
            }

            // Tentar caminho alternativo
            if (url == null) {
                String caminhoAlternativo = caminho.replace("/packt/frameworks/nexusfx/AlertSystem/fx/assets/sound/",
                        "/com/ossobo/nexusfx/assets/sound/");
                url = AlertaSons.class.getResource(caminhoAlternativo);
            }

            // Tentar fallback para WAV
            if (url == null) {
                String caminhoWAV = caminho.replace(".mp3", ".wav");
                url = AlertaSons.class.getResource(caminhoWAV);
            }

            if (url != null) {
                registrarSom(nome, url);
            }

        } catch (Exception e) {
            // Silencioso
        }
    }

    /** Tocar som baseado no tipo de alerta */
    public static void tocarSom(TipoAlerta tipo) {
        if (!inicializado) {
            inicializar();
        }

        String nomeSom = tipo.name().toLowerCase();
        tocarSomPorNome(nomeSom);
    }

    /** Tocar som de confirmação baseado no tipo */
    public static void tocarSomConfirmacao(TipoConfirmacao tipo) {
        if (!inicializado) {
            inicializar();
        }

        String nomeSom = "confirmacao_" + tipo.name().toLowerCase();

        // Se não houver som específico, usar padrão
        if (!somDisponivel(nomeSom)) {
            nomeSom = "confirmation";
        }

        tocarSomPorNome(nomeSom);
    }

    /** Método interno para tocar som por nome */
    private static void tocarSomPorNome(String nomeSom) {
        javafx.application.Platform.runLater(() -> {
            // Tentar MediaPlayer primeiro (MP3)
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

            // Tentar fallback genérico
            if (nomeSom.startsWith("confirmacao_")) {
                tocarSomPorNome("confirmation");
            }
        });
    }

    /** Tocar som personalizado */
    public static void tocarSomPersonalizado(String caminhoOuId) {
        if (!inicializado) {
            inicializar();
        }

        javafx.application.Platform.runLater(() -> {
            try {
                URL url = null;

                // Tentar como ID do ResourceAPI
                if (resourceAPI != null && resourceAPI.exists(caminhoOuId, ResourceType.SOUND)) {
                    url = resourceAPI.getSoundUrl(caminhoOuId);
                }

                // Fallback: tentar como caminho
                if (url == null) {
                    url = AlertaSons.class.getResource(caminhoOuId);
                }

                if (url != null) {
                    String urlString = url.toString();
                    if (urlString.toLowerCase().endsWith(".mp3")) {
                        Media media = new Media(urlString);
                        MediaPlayer player = new MediaPlayer(media);
                        player.setVolume(volumeGeral);
                        player.setCycleCount(1);
                        player.play();
                    } else {
                        AudioClip clip = new AudioClip(urlString);
                        clip.setVolume(volumeGeral);
                        clip.play();
                    }
                }
            } catch (Exception e) {
                // Silencioso
            }
        });
    }

    /** Define volume geral (0.0 a 1.0) */
    public static void setVolumeGeral(double volume) {
        volumeGeral = Math.max(0.0, Math.min(1.0, volume));

        mediaPlayers.values().forEach(player ->
                player.setVolume(volumeGeral)
        );

        audioClips.values().forEach(clip ->
                clip.setVolume(volumeGeral)
        );
    }

    /** Para todos os sons */
    public static void pararTodos() {
        mediaPlayers.values().forEach(player -> {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.stop();
            }
        });
    }

    /** Para um som específico */
    public static void pararSom(String nome) {
        MediaPlayer player = mediaPlayers.get(nome);
        if (player != null && player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.stop();
        }
    }

    /** Verifica se um som está disponível */
    public static boolean somDisponivel(String nome) {
        // Verificar cache local
        if (mediaPlayers.containsKey(nome) || audioClips.containsKey(nome)) {
            return true;
        }

        // Verificar ResourceAPI
        String resourceId = SOM_PARA_RESOURCE_ID.get(nome);
        return resourceAPI != null && resourceId != null &&
                resourceAPI.exists(resourceId, ResourceType.SOUND);
    }

    /** Pré-carrega um som específico */
    public static void preCarregarSom(String nome, String caminho) {
        carregarSom(nome, caminho);
    }

    /** Remove um som da memória */
    public static void liberarSom(String nome) {
        MediaPlayer player = mediaPlayers.remove(nome);
        if (player != null) {
            player.dispose();
        }
        audioClips.remove(nome);
    }

    /** Reinicializa o sistema de sons */
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
     * ✅ Diagnóstico do sistema de sons
     */
    public static void diagnosticar() {
        System.out.println("\n🔊 ALERTA SONS - DIAGNÓSTICO");
        System.out.println("=".repeat(50));
        System.out.println("• ResourceAPI: " + (resourceAPI != null ? "✅ Vinculado" : "❌ Não vinculado"));
        System.out.println("• Inicializado: " + inicializado);
        System.out.println("• Volume: " + (volumeGeral * 100) + "%");
        System.out.println("• MediaPlayers: " + mediaPlayers.size());
        System.out.println("• AudioClips: " + audioClips.size());

        if (resourceAPI != null) {
            System.out.println("\n📋 SONS NO RESOURCE API:");
            resourceAPI.listIdsByType(ResourceType.SOUND).forEach(id ->
                    System.out.println("  • " + id + " → " + resourceAPI.exists(id))
            );
        }
        System.out.println("=".repeat(50));
    }
}
