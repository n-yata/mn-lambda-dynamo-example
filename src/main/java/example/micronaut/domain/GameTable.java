package example.micronaut.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class GameTable implements Serializable {

    public GameTable() {
    }

    public GameTable(String gameCategory, String gameId, String gameTitle, String publishDate) {
        super();
        this.gameCategory = gameCategory;
        this.gameId = gameId;
        this.gameTitle = gameTitle;
        this.publishDate = publishDate;
    }

    private static final long serialVersionUID = 1L;

    /** カテゴリ */
    private String gameCategory;
    /** ID */
    private String gameId;
    /** タイトル */
    private String gameTitle;
    /** 発売日 */
    private String publishDate;
}
