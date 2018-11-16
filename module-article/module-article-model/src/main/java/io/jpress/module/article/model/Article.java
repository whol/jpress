/**
 * Copyright (c) 2016-2019, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jpress.module.article.model;

import com.jfinal.core.JFinal;
import io.jboot.db.annotation.Table;
import io.jboot.utils.StrUtils;
import io.jpress.JPressConsts;
import io.jpress.JPressOptions;
import io.jpress.commons.utils.CommonsUtils;
import io.jpress.commons.utils.JsoupUtils;
import io.jpress.commons.utils.MarkdownUtils;
import io.jpress.module.article.model.base.BaseArticle;

import java.util.List;

/**
 * Generated by Jboot.
 */
@Table(tableName = "article", primaryKey = "id")
public class Article extends BaseArticle<Article> {

    public static final String STATUS_NORMAL = "normal";
    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_TRASH = "trash";


    public boolean isNormal() {
        return STATUS_NORMAL.equals(getStatus());
    }

    public boolean isDraft() {
        return STATUS_DRAFT.equals(getStatus());
    }

    public boolean isTrash() {
        return STATUS_TRASH.equals(getStatus());
    }

    public String getHtmlView() {
        return StrUtils.isBlank(getStyle()) ? "article.html" : "article_" + getStyle().trim() + ".html";
    }


    public String getUrl() {
        String link = getLinkTo();
        if (StrUtils.isNotBlank(link)) {
            return link;
        }

        if (StrUtils.isBlank(getSlug())) {
            return JFinal.me().getContextPath() + "/article/" + getId() + JPressOptions.getAppUrlSuffix();
        } else {
            return JFinal.me().getContextPath() + "/article/" + getSlug() + JPressOptions.getAppUrlSuffix();
        }
    }

    public boolean isCommentEnable() {
        Boolean cs = getCommentStatus();
        return cs != null && cs == true;
    }

    public String getText() {
        return JsoupUtils.getText(getContent());
    }

    @Override
    public String getContent() {
        String content = super.getContent();
        if (JPressConsts.EDIT_MODE_MARKDOWN.equals(getEditMode())) {
            content = MarkdownUtils.toHtml(content);
        }
        return JsoupUtils.makeImageSrcToAbsolutePath(content, JPressOptions.getResDomain());
    }


    public String _getOriginalContent() {
        return super.getContent();
    }


    public List<String> getImages() {
        return JsoupUtils.getImageSrcs(getContent());
    }


    public String getFirstImage() {
        return JsoupUtils.getFirstImageSrc(getContent());
    }

    public String getShowImage() {
        String thumbnail = getThumbnail();
        return StrUtils.isNotBlank(thumbnail) ? thumbnail : getFirstImage();
    }

    @Override
    public boolean save() {
        CommonsUtils.preventingXssAttacks(this, "content");
        return super.save();
    }

    @Override
    public boolean update() {
        CommonsUtils.preventingXssAttacks(this, "content");
        return super.update();
    }


}
