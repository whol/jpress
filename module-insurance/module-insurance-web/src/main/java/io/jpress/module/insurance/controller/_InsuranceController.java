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
package io.jpress.module.insurance.controller;

import com.google.inject.Inject;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.utils.ArrayUtils;
import io.jboot.utils.StrUtils;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.controller.validate.EmptyValidate;
import io.jboot.web.controller.validate.Form;
import io.jpress.core.menu.annotation.AdminMenu;
import io.jpress.core.template.TemplateManager;
import io.jpress.module.insurance.model.Insurance;
import io.jpress.module.insurance.model.InsuranceCalc;
import io.jpress.module.insurance.model.InsuranceType;
import io.jpress.module.insurance.service.InsuranceCalcService;
import io.jpress.module.insurance.service.InsuranceService;
import io.jpress.module.insurance.service.InsuranceTypeService;
import io.jpress.web.base.AdminControllerBase;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * @version V1.0
 * @Package io.jpress.module.page.controller.admin
 */
@RequestMapping("/admin/insurance")
public class _InsuranceController extends AdminControllerBase {

    @Inject
    private InsuranceService insuranceService;
    @Inject
    private InsuranceTypeService insuranceTypeService;
    @Inject
    private InsuranceCalcService insuranceCalcService;


    @AdminMenu(text = "保险管理", groupId = "insurance", order = 1)
    public void index() {

        Page<Insurance> insurance = (Page<Insurance>) insuranceService.paginate(getPagePara(), 10);

        setAttr("insurance", insurance);

        render("insurance/insurance_list.html");
    }

    @AdminMenu(text = "新建保险", groupId = "insurance", order = 2)
    public void write() {
        int insuranceId = getParaToInt(0, 0);

        Insurance insurance = insuranceId > 0 ? insuranceService.findById(insuranceId) : null;
        setAttr("insurance", insurance);

        List<InsuranceType> insuranceTypeList = insuranceTypeService.findAll();
        setAttr("insuranceTypeList", insuranceTypeList);

        render("insurance/insurance_write.html");
    }

    @AdminMenu(text = "保险类型管理", groupId = "insurance", order = 3)
    public void insuranceType() {

        Page<InsuranceType> insuranceType = (Page<InsuranceType>) insuranceTypeService.paginate(getPagePara(), 10);

        setAttr("insuranceType", insuranceType);

        render("insurance/insurance_type_list.html");
    }


    @AdminMenu(text = "新建保险类型", groupId = "insurance", order = 4)
    public void typeWrite() {
        int insuranceTypeId = getParaToInt(0, 0);

        InsuranceType insuranceType = insuranceTypeId > 0 ? insuranceTypeService.findById(insuranceTypeId) : null;
        setAttr("insuranceType", insuranceType);

        render("insurance/insurance_type_write.html");
    }

    @EmptyValidate({
            @Form(name = "insuranceType.insuranceTypeName", message = "名称不能为空"),
            @Form(name = "insuranceType.paymentType", message = "交费类型不能为空")
    })
    public void doTypeWriteSave() {
        InsuranceType insuranceType = getModel(InsuranceType.class, "insuranceType");


        insuranceTypeService.saveOrUpdate(insuranceType);
        renderJson(Ret.ok().set("id", insuranceType.getInsuranceTypeId()));
    }

    public void doWriteSave() {
        InsuranceCalc insuranceCalc = getModel(InsuranceCalc.class, "insuranceCalc");
        insuranceCalcService.saveOrUpdate(insuranceCalc);
        //renderJson(Ret.ok().set("calcId", insuranceCalc.getCalcId()));
        //目标保险年数
        Integer insuranceYear = 15;
        //目标保险年利率
        BigDecimal yearLilv = insuranceCalc.getTargetLilv().divide(BigDecimal.valueOf(100));
        //目标保险月利率
        BigDecimal monthLilv = yearLilv.divide(BigDecimal.valueOf(12), 10, BigDecimal.ROUND_HALF_EVEN);

        //来源保险
        Insurance sourceInsurance = new Insurance();
        sourceInsurance.setAnnualPremium(insuranceCalc.getSourceAnnualPremium());
        sourceInsurance.setInsuranceTypeId(insuranceCalc.getSourceTypeId());
        InsuranceType sourceType = insuranceTypeService.findById(insuranceCalc.getSourceTypeId());
        sourceInsurance.setInsuranceTypeName(sourceType.getInsuranceTypeName());
        sourceInsurance.setPaymentAge(sourceType.getPaymentAge());
        //基本保险金额
        BigDecimal basicInsuranceAmount = insuranceCalc.getSourceAnnualPremium().multiply(BigDecimal.valueOf(0.71623));
        sourceInsurance.setBasicInsuranceAmount(basicInsuranceAmount);

        //目标保险
        Insurance targetInsurance = new Insurance();
        targetInsurance.setAnnualPremium(insuranceCalc.getTargetAnnualPremium());
        targetInsurance.setInsuranceTypeId(insuranceCalc.getTargetTypeId());
        InsuranceType targetType = insuranceTypeService.findById(insuranceCalc.getTargetTypeId());
        targetInsurance.setInsuranceTypeName(targetType.getInsuranceTypeName());
        targetInsurance.setPaymentAge(targetType.getPaymentAge());

        //每个保险年度第一天进行缴费
        if (null == sourceInsurance.getInsuranceAmount()) {
            sourceInsurance.setInsuranceAmount(BigDecimal.ZERO);
        }
        if (null == targetInsurance.getInsuranceAmount()) {
            targetInsurance.setInsuranceAmount(BigDecimal.ZERO);
        }
        /*for (int i=0; i<sourceInsurance.getPaymentAge(); i++) {
            sourceInsurance.setInsuranceAmount(sourceInsurance.getInsuranceAmount().add(sourceInsurance.getAnnualPremium()));
        }
        for (int i=0; i<targetInsurance.getPaymentAge(); i++) {
            targetInsurance.setInsuranceAmount(targetInsurance.getInsuranceAmount().add(targetInsurance.getAnnualPremium()));
        }*/

        //特别生存保险金 = 年交保险费 * 0.5
        BigDecimal halfSurvivorshipInsurance = sourceInsurance.getAnnualPremium().divide(BigDecimal.valueOf(2));

        //生存保险金 = 基本保险金额 * 0.3
        BigDecimal survivorshipInsurance = sourceInsurance.getBasicInsuranceAmount().multiply(BigDecimal.valueOf(0.3)).setScale(6, BigDecimal.ROUND_HALF_EVEN);

        //满期生存保险金 = 基本保险金额 * 1
        BigDecimal fullSurvivorshipInsurance = sourceInsurance.getBasicInsuranceAmount();

        //返还按15年计算
        for (Integer i=0; i<insuranceYear; i++) {
            System.out.println("第" + (i+1) + "年：");

            if (i < sourceInsurance.getPaymentAge()) {
                sourceInsurance.setInsuranceAmount(sourceInsurance.getInsuranceAmount().add(sourceInsurance.getAnnualPremium()));
            }

            if (i < targetInsurance.getPaymentAge()) {
                targetInsurance.setInsuranceAmount(targetInsurance.getInsuranceAmount().add(targetInsurance.getAnnualPremium()));
            }

            //从第二个月开始计利息
            Boolean start = false;
            if (i < 5) {
            } else if (i == 5 || i == 6) {
                //第6年的第一天是满五周年
                System.out.println("第 " + i + " 个保单周年日 50");
                //保险金额转移到目标保险中
                sourceInsurance.setInsuranceAmount(sourceInsurance.getInsuranceAmount().subtract(halfSurvivorshipInsurance));
                targetInsurance.setInsuranceAmount(targetInsurance.getInsuranceAmount().add(halfSurvivorshipInsurance));
                if (i == 6) {
                    //保单持续奖励
                    targetInsurance.setInsuranceAmount(targetInsurance.getInsuranceAmount().add(halfSurvivorshipInsurance.divide(BigDecimal.valueOf(10))));
                }
            } else if (i == insuranceYear - 1) {
                //最后一年
                System.out.println("第 " + i + " 个保单周年日 100");
                //保险金额转移到目标保险中
                sourceInsurance.setInsuranceAmount(sourceInsurance.getInsuranceAmount().subtract(fullSurvivorshipInsurance));
                targetInsurance.setInsuranceAmount(targetInsurance.getInsuranceAmount().add(fullSurvivorshipInsurance));
            } else if (i < insuranceYear - 1){
                //第7个保单周年日
                System.out.println("第 " + i + " 个保单周年日 30");
                //保险金额转移到目标保险中
                sourceInsurance.setInsuranceAmount(sourceInsurance.getInsuranceAmount().subtract(survivorshipInsurance));
                targetInsurance.setInsuranceAmount(targetInsurance.getInsuranceAmount().add(survivorshipInsurance));
                //保单持续奖励
                targetInsurance.setInsuranceAmount(targetInsurance.getInsuranceAmount().add(survivorshipInsurance.divide(BigDecimal.valueOf(10))));
            }

            //来源保险内金额
            System.out.println("来源保险内金额 = " + sourceInsurance.getInsuranceAmount());
            //目标保险内金额(年初)
            System.out.println("目标保险内金额(年初) = " + targetInsurance.getInsuranceAmount());

            //目标保险月复利
            for (int j=0; j<12; j++) {
                if (!start) {
                    start = true;
                    continue;
                }
                targetInsurance.setInsuranceAmount(targetInsurance.getInsuranceAmount().multiply(BigDecimal.ONE.add(monthLilv)).setScale(6, BigDecimal.ROUND_HALF_EVEN));
                //System.out.println("第 " + (j+1) + " 月初，累计金额为 " + targetInsurance.getInsuranceAmount());
            }
            //目标保险内金额(年底)
            System.out.println("目标保险内金额(年底) = " + targetInsurance.getInsuranceAmount());
        }


        //第6年的第一天是满五周年

        //第7年的第一天是满六周年

    }


}
