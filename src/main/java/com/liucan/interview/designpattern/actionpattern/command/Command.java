package com.liucan.interview.designpattern.actionpattern.command;

/**
 * 命令（command）模式:将命令的调用者和实现者通过命令对象来将其分开，可将执行的命令记录保存，
 * 将命令对象进行储存、传递、调用、增加与管理，可以对行为撤销，记录、重做
 * <p>
 * 命令模式的主要优点如下。
 * 能将调用操作的对象与实现该操作的对象解耦。
 * 增加或删除命令非常方便。采用命令模式增加与删除命令不会影响其他类，它满足“开闭原则”，对扩展比较灵活。
 * 可以实现宏命令。命令模式可以与组合模式结合，将多个命令装配成一个组合命令，即宏命令。
 * 方便实现 Undo 和 Redo 操作。命令模式可以与后面介绍的备忘录模式结合，可以将命令对象存储起来 实现命令的撤销与恢复
 *
 * @author liucan
 * @version 19-3-28
 */
public interface Command {
    void executed();
}
