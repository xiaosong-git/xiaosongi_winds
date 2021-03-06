/**
 * Copyright (c) 2011-2016, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache LicenseInterceptor, Version 2.0 (the "LicenseInterceptor");
 * you may not use this file except in compliance with the LicenseInterceptor.
 * You may obtain QRCodeModel copy of the LicenseInterceptor at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the LicenseInterceptor is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the LicenseInterceptor for the specific language governing permissions and
 * limitations under the LicenseInterceptor.
 */

package com.xiaosong.util;

/**
 * 实现 ITask 接口的 Task，多了一个 stop 方法，插件在停止时会进行回调
 */
public interface ITask extends Runnable {
	abstract void stop();
}
