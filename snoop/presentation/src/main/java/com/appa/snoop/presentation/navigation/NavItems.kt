package com.appa.snoop.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.appa.snoop.presentation.R
import com.appa.snoop.presentation.navigation.Router.MAIN_CATEGORY_ROUTER_NAME
import com.appa.snoop.presentation.navigation.Router.MAIN_HOME_ROUTER_NAME
import com.appa.snoop.presentation.navigation.Router.MAIN_LIKE_ITEM_ROUTER_NAME
import com.appa.snoop.presentation.navigation.Router.MAIN_MY_PAGE_ROUTER_NAME
import com.appa.snoop.presentation.navigation.Router.Title.MAIN_CATEGORY
import com.appa.snoop.presentation.navigation.Router.Title.MAIN_HOME
import com.appa.snoop.presentation.navigation.Router.Title.MAIN_LIKE_ITEM
import com.appa.snoop.presentation.navigation.Router.Title.MAIN_MY_PAGE

interface Destination {
    val route: String
    val title: String
}

// 네비게이션 바 구성
sealed class MainNav(
    override val route: String,
    @DrawableRes val icon: Int,
    override val title: String
) : Destination {

    object Home : MainNav(MAIN_HOME_ROUTER_NAME, R.drawable.ic_home, MAIN_HOME)
    object Category : MainNav(MAIN_CATEGORY_ROUTER_NAME, R.drawable.ic_category, MAIN_CATEGORY)
    object Like : MainNav(MAIN_LIKE_ITEM_ROUTER_NAME, R.drawable.ic_like, MAIN_LIKE_ITEM)
    object MyPage : MainNav(MAIN_MY_PAGE_ROUTER_NAME, R.drawable.ic_mypage, MAIN_MY_PAGE)

    companion object {
        fun isMainRoute(route: String?): Boolean {
            return when (route) {
                MAIN_HOME_ROUTER_NAME, MAIN_CATEGORY_ROUTER_NAME, MAIN_LIKE_ITEM_ROUTER_NAME, MAIN_MY_PAGE_ROUTER_NAME -> true

                else -> false
            }
        }
    }
}

object SearchNav : Destination {
    override val route: String = Router.MAIN_SEARCH_ROUTER_NAME
    override val title: String = Router.Title.MAIN_SEARCH
}

// 추가 화면 코드 아래에 작성