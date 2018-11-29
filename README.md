# DragLayout
Android拖拽布局，包括以下布局：
	DragLayout 基础布局类
	FlowLayout 可折叠布局，继承DragLayout
	DragRefreshLayout 拖拽刷新布局，继承DragLayout
	VerticalRefreshLayout 垂直刷新布局，继承DragRefreshLayout

## DragLayout
基础布局类，后面的布局都是继承此类。主要实现了上下左右四边的拖拽。

## FlowLayout
可折叠布局，优化四边的打开条件，可以实现类似商品详情或者列表ITEM左右拖拽菜单效果。

## DragRefreshLayout
拖拽刷新布局，主要用于分页加载或者刷新，但一般不直接使用此类，此类作为分页加载基础类。

## VerticalRefreshLayout
垂直刷新布局，继承DragRefreshLayout，实现了分页加载和刷新功能，具体使用方式请看例子。
