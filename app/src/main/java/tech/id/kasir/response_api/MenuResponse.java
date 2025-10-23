package tech.id.kasir.response_api;

import java.util.List;

public class MenuResponse {
    private boolean status;
    private List<Menu> menus;

    public boolean isStatus() { return status; }
    public List<Menu> getMenus() { return menus; }
}
