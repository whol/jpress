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
package io.jpress;

import io.jpress.codegen.ModuleGenerator;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * @version V1.0
 * @Package io.jboot.codegen
 */
public class InsuranceModuleGenerator {


    private static String dbUrl = "jdbc:mysql://47.99.240.128:3306/newjpress";
    private static String dbUser = "jpress";
    private static String dbPassword = "jpress19851210";


    private static String moduleName = "insurance";
    private static String dbTables = "insurance_calc";
    private static String modelPackage = "io.jpress.module.insurance.model";
    private static String servicePackage = "io.jpress.module.insurance.service";

    public static void main(String[] args) {

        ModuleGenerator moduleGenerator = new ModuleGenerator(moduleName, dbUrl, dbUser, dbPassword, dbTables, modelPackage, servicePackage);
        moduleGenerator.gen();

    }
}
