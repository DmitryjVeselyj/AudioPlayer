package com.polyap.music_player.interfaces;

/**
 * интерфейс для сервиса в строке уведомлений
 */
public interface ActionPlaying {
    /**
     * нажатие кнопки проигрывания
     */
    void btn_play_pauseClicked();

    /**
     * нажатие кнопки следующего трека
     */
    void btn_nextClicked();

    /**
     * нажатие кнопки предыдущего трека
     */
    void btn_prevClicked();

    /**
     * нажатие кнопки "крестик", чтобы убирать сервис из строки уведомлений
     */
    void btn_dismiss();
}
