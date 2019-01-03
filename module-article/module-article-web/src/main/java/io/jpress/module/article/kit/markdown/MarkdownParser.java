package io.jpress.module.article.kit.markdown;

import com.jfinal.log.Log;
import io.jboot.utils.FileUtils;
import io.jpress.JPressConsts;
import io.jpress.commons.utils.MarkdownUtils;
import io.jpress.module.article.model.Article;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Wang（i@ryanc.cc）
 * @version V1.0
 * @Package io.jpress.module.article.kit.markdown
 */
public class MarkdownParser {

    private static final Log log = Log.getLog(MarkdownParser.class);
    private Article article = new Article();

    /**
     * markdown文本
     */
    private String markdown = "";

    /**
     * markdown文档的元数据
     */
    private Map<String, List<String>> datas = new HashMap<>();

    /**
     * 单个节点的值
     */
    private List<String> elementValue = new ArrayList<>();

    /**
     * 日期格式化
     */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取文章
     *
     * @return Article
     */
    public Article getArticle() throws ParseException {
        article.setContent(markdown);
        article.setEditMode(JPressConsts.EDIT_MODE_MARKDOWN);
        article.setStatus(Article.STATUS_NORMAL);

        for (String key : datas.keySet()) {
            elementValue = datas.get(key);
            for (String ele : elementValue) {
                if ("title".equals(key)) {
                    article.setTitle(ele);
                } else if ("date".equals(key)) {
                    article.setCreated(sdf.parse(ele));
                } else if ("updated".equals(key)) {
                    article.setModified(sdf.parse(ele));
                }
            }
        }
        return article;
    }

    /**
     * 解析markdown文档
     *
     * @param mdFile mdFile
     */
    public void parse(File mdFile) {
        try {
            markdown = FileUtils.readString(mdFile);
            datas = MarkdownUtils.getFrontMatter(markdown);
        } catch (Exception e) {
            log.warn("ConfigParser parser exception", e);
        }
    }
}
