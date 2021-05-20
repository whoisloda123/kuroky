package com.liucan.interview.designpattern.actionpattern.mediator;

/**
 * @author liucan
 * @version 19-3-31
 */
public class ColleagueA extends Colleague {

    public ColleagueA(Mediator mediator) {
        super(mediator);
        mediator.register(this);
    }

    @Override
    public void send() {
        mediator.relay(this);
    }

    @Override
    public void receive() {
        System.out.println("收到同事的请求了");
    }
}
