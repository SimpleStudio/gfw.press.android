/**
 * GFW.Press
 * Copyright (C) 2016  chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package press.gfw.android;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeoutException;

import press.gfw.Client;

/**
 * GFW.Press Android 客户端组件
 *
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 */
public class SettingsActivity extends CompatActivity {

    private ProxyAPN proxyAPN = null;

    private ProxyWIFI proxyWIFI = null;

    private Client client = null;

    private String proxyPort = "";

    /**
     * 配置监听器
     */
    private Preference.OnPreferenceChangeListener valueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String key = preference.getKey();

            String stringValue = value.toString();

            switch (key) {

                case "text_password":

                    String stars = "***********************************";

                    preference.setSummary(stars.substring(0, (stringValue.length() > stars.length() ? stars.length() : stringValue.length())));

                    break;

                default:

                    preference.setSummary(stringValue);

                    break;

            }

            return true;

        }

    };

    /**
     * 开关监听器
     */
    private Preference.OnPreferenceChangeListener switchListener = new Preference.OnPreferenceChangeListener() {

        @SuppressWarnings("deprecation")
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            if ("switch".equals(preference.getKey())) {
                if ((boolean) newValue) {

                    log("运行开关打开");

                    start();

                } else {

                    log("运行开关关闭");

                    stop();

                }
            }
            if ("setup_proxy".equals(preference.getKey())) {
                if ((boolean) newValue) {

                    log("配置代理打开");

                    // start auto proxy setup
                    setupProxy();

                } else {

                    log("配置代理关闭");

                    // start auto proxy clear
                    clearProxy();

                }
            }

            return true;

        }

    };

    /**
     * 停止运行
     */
    private void stop() {

        closeProxy();

        if (client != null && !client.isKill()) {

            client.kill();

        }

    }

    /**
     * 关闭全局代理
     */
    private void closeProxy() {

        if (proxyAPN == null) {

            proxyAPN = new ProxyAPN(getApplicationContext());

        }

        proxyAPN.closeProxy();

        if (proxyWIFI == null) {

            proxyWIFI = new ProxyWIFI(getApplicationContext());

        }

        proxyWIFI.closeProxy();

        log("已关闭全局代理");

    }

    /**
     * 打印信息
     *
     * @param o 打印对象
     */
    @SuppressWarnings("unused")
    private void log(Object o) {

        String time = (new Timestamp(System.currentTimeMillis())).toString().substring(0, 19);

        System.out.println("[" + time + "] " + o.toString());

    }

    /**
     * 打开全局代理
     */
    private void openProxy() {

        if (proxyPort == null || !proxyPort.matches("\\d+")) {

            return;

        }

        if (proxyAPN == null) {

            proxyAPN = new ProxyAPN(getApplicationContext());

        }

        proxyAPN.openProxy(Integer.valueOf(proxyPort));

        if (proxyWIFI == null) {

            proxyWIFI = new ProxyWIFI(getApplicationContext());

        }

        proxyWIFI.openProxy(Integer.valueOf(proxyPort));

        log("已打开全局代理");

    }

    /**
     * 开始运行
     */
    @SuppressWarnings("deprecation")
    private void start() {

        String serverHost = getValue(findPreference("text_server"));

        String serverPort = getValue(findPreference("text_port"));

        String password = getValue(findPreference("text_password"));

        proxyPort = getValue(findPreference("text_listen_port"));

        log("配置信息：");

        log("serverHost: " + serverHost);

        log("serverPort: " + serverPort);

        log("password: " + password);

        log("proxyPort: " + proxyPort);

        openProxy();

        if (client != null && !client.isKill()) {

            if (serverHost.equals(client.getServerHost()) && serverPort.equals(String.valueOf(client.getServerPort())) && password.equals(client.getPassword()) && proxyPort.equals(String.valueOf(client.getListenPort()))) {

                return;

            } else {

                client.kill();

            }

        }

        client = new Client(serverHost, serverPort, password, proxyPort);

        client.start();

    }

    private void setupProxy() {
        try {
            String setupCmd = String.format("am start -n %s/tk.elevenk.proxysetter.MainActivity -e host %s -e port %s",
                    getPackageName(),
                    "127.0.0.1",
                    "3128");
            RootTools.getShell(false).add(new Command(0, setupCmd));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
    }

    private void clearProxy() {
        try {
            String clearCmd = String.format("am start -n %s/tk.elevenk.proxysetter.MainActivity -e clear true",
                    getPackageName());
            RootTools.getShell(false).add(new Command(0, clearCmd));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置开关监听器
     *
     * @param preference Preference
     */
    private void setSwitchListener(Preference preference) {

        preference.setOnPreferenceChangeListener(switchListener);

        switchListener.onPreferenceChange(preference, ((SwitchPreference) preference).isChecked());

    }

    /**
     * 设置配置监听器
     *
     * @param preference Preference
     */
    private void setValueListener(Preference preference) {

        preference.setOnPreferenceChangeListener(valueListener);

        valueListener.onPreferenceChange(preference, getValue(preference));

    }

    /**
     * 获取配置信息
     *
     * @param preference Preference
     * @return 值
     */
    private String getValue(Preference preference) {

        return PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), "");

    }

    /**
     * @param savedInstanceState InstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setupActionBar();

    }

    private void setupActionBar() {

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

            // actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);

        }

    }

    /**
     * @param savedInstanceState InstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);

        setListener();

    }

    /**
     * 设置开关和配置监听器
     */
    @SuppressWarnings("deprecation")
    private void setListener() {

        addPreferencesFromResource(R.xml.pref_general);

        setValueListener(findPreference("text_server"));
        setValueListener(findPreference("text_port"));
        setValueListener(findPreference("text_password"));
        setValueListener(findPreference("text_listen_port"));

        setSwitchListener(findPreference("switch"));
        setSwitchListener(findPreference("setup_proxy"));

    }


    /**
     * @param target Target
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {

        loadHeadersFromResource(R.xml.pref_headers, target);

    }


}
