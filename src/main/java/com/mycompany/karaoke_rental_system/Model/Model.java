package com.mycompany.karaoke_rental_system.Model;

import com.mycompany.karaoke_rental_system.ViewFactory;

public class Model {

    private static Model model;
    private final ViewFactory viewFactory;
    private Integer userid;
    private String userRole;

    private Model() {
        this.viewFactory = new ViewFactory();
    }

    public static synchronized Model getInstance() {
        if (model == null) {
            model = new Model();

        }
        return model;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public int getcurrentuserid() {
        return userid;
    }

    public void setcurrentuserid(Integer userid) {
        this.userid = userid;
    }
    public String getRole(){
        return userRole;
    }
    public void setRole(String role){
        this.userRole = role;
    }

    public void clearUserData() {
        this.userid = null;

        if (viewFactory != null) {
            viewFactory.clearViews();
        }
    }

}
