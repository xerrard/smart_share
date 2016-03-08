package org.xerrard.smartshare.Present.State;

/**
 * 类描述：
 * 创建人：xuqiang
 * 创建时间：16-3-8 下午3:46
 * 修改人：xuqiang
 * 修改时间：16-3-8 下午3:46
 * 修改备注：
 */
public interface IBaseState {
    int getValue();
    String getDisplayString();
    int UNDEFINED_VALUE = 0x99;
}
