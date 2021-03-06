package com.liucan.kuroky.designpattern.actionpattern.status.statusenum;

import com.liucan.kuroky.designpattern.actionpattern.status.ThreadContext;
import com.liucan.kuroky.designpattern.actionpattern.status.command.ThreadCommand;

/**
 * @author liucan
 * @version 19-3-31
 */
public class BlockedThreadStatus extends AbstractThreadStatus {
    @Override
    public void handle(ThreadContext context) {
        ThreadCommand command = context.getCommand();
        if (command.type() == ThreadCommand.TYPE.RESUME) {
            command.executed();
            System.out.println("暂停状态，准备转换为就绪状态");
            context.changeStatus(context.getStatus(ThreadStatus.RUNNABLE));
        } else {
            System.out.println("不是resume命令，不感兴趣");
        }
    }
}
