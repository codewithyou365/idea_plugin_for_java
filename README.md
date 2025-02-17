[English Version](README-EN.md)

# 点一下star✨，是对作者最大的支持

**支持的IDEA版本为 2022.3.3 及以上**

**如果成都有工作机会，请与我联系 codewithyou365@gmail.com**

**使用说明**

- **功能 1**
    - `.full`

  ![SQL 自动补全](https://media.githubusercontent.com/media/codewithyou365/idea_plugin_for_java/refs/heads/main/src/main/resources/gif/AutoFullSqlSelect.gif)

  **使用场景：**
  在项目中大量使用 SQL 进行查询时，通常会使用 `SELECT *`，但这样不能做到精准查询，导致性能下降。

  **原理：**
  当声明 `String sOrder` 时，程序会自动检查下文所有 `order` 对象的 `get*` 方法，并根据使用情况为 `sOrder` 赋值。其中 `s` 代表 `select`。

- **功能 2**
    - `.autoCheckState`

  ![状态检查](https://media.githubusercontent.com/media/codewithyou365/idea_plugin_for_java/refs/heads/main/src/main/resources/gif/CheckState.gif)

  **使用场景：**
  对状态推进进行检查，对于 声明的状态推进逻辑 和 实现的状态推进逻辑，这两者可能存在不一致的情况，需要进行自动检查。

  **原理：**
  使用 `.autoCheckState` 命令后，会将代码中的实现的推进逻辑转换为字符串，并调用 `checkState` 方法进行校验。其中checkState方法需要使用者自己实现。

- **功能 3**
    - `Call Get`

  ![调用远程接口](https://media.githubusercontent.com/media/codewithyou365/idea_plugin_for_java/refs/heads/main/src/main/resources/gif/CallRemoteAction.gif)

  **使用场景：**
  在 Spring 项目部署到开发环境后，你希望将开发环境的某个接口的流量引导到本地进行调试。你可以通过该功能完成流量引导的配置。

  **转发方案：(不是插件本身功能)**
    - **nacos：** 会将所有流量引导至本地。
    - **kt-connect：** 需要修改客户端以指定 `VERSION`。
    - **自己定义转发：** 但在大多数情况下，我们只关注某一个接口的调试，因此更推荐在网关中实现流量转发方法以及配置接口。

  **原理：**
  该功能会根据当前 Run Configuration 的环境变量 `__CALL_TEMPLATE`（如 `https://x.com/{path}` ），
  自动替换 `{path}` 为 Spring Controller 中的 Path，并发送 GET 请求。

---

## 其他:

  [例子](https://github.com/codewithyou365/idea_plugin_for_java/tree/main/src/main/java/org/codewithyou365/easyjava/example)

