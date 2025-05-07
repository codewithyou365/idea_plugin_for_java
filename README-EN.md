[中文版](README.md)

# Clicking the star ✨ is the greatest support for the author.

**Supported IntelliJ IDEA Versions: 2022.3.3 and Above**

**Usage Instructions**

- **Function 1**
  - `.full`

  ![.full](https://media.githubusercontent.com/media/codewithyou365/idea_plugin_for_java/refs/heads/main/src/main/resources/gif/AutoFullSqlSelect.gif)

  **Use Case:**
  When using SQL extensively in a project, developers often write SELECT *, which is not precise and can lead to performance degradation.

  **How It Works:**
  When a String sOrder is declared, the program automatically checks all order objects’ get* methods in the following code and assigns a value to sOrder based on their usage. The s prefix stands for select.

- **Function 2**
  - `.autoCheckState`

  ![.autoCheckState](https://media.githubusercontent.com/media/codewithyou365/idea_plugin_for_java/refs/heads/main/src/main/resources/gif/CheckState.gif)

  **Use Case:**
  Check the state progression. There may be inconsistencies between the declared state progression logic and the implemented state progression logic, which need to be automatically checked.

  **How It Works:**
  When the .autoCheckState command is used, the implemented advancement logic in the code is converted into a string and validated using the checkState method, and the checkState method needs to be implemented by user.


- **Function 3**
  - `Call Get`

  ![Call Get](https://media.githubusercontent.com/media/codewithyou365/idea_plugin_for_java/refs/heads/main/src/main/resources/gif/CallRemoteAction.gif)

  **Use Case:**
  After the Spring project is deployed to the DEV environment, you may want to redirect the traffic of a certain interface in the DEV environment to your local machine for debugging. You can configure the traffic redirection using this feature.

  **Forwarding plan: (not the plugin functionality itself)**
  - **Nacos:** Redirects all traffic to the local environment.
  - **kt-connect:** Requires modifying the client to specify the VERSION.
  - **Define by self:** However, in most cases, only a specific API needs debugging. Thus, it is more recommended to implement traffic forwarding methods and configuration interfaces in the gateway.

  **How It Works:**
  This feature automatically generates and sends a GET request based on two environment variables defined in the current Run Configuration. For example:
  -	Environment variable __CALL_TEMPLATE: https://x.com/{path}
  -	Environment variable __CALL_TEMPLATE_HEADER: X-Api-Path:{path}
  
  **The feature will automatically parse the path from the Spring Controller, replace the {path} placeholder in the templates, and send a GET request using the resulting URL.**
  **Assuming the parsed {path} is user/list, the resulting request would be equivalent to the following cURL command:**
  - curl -X GET "https://x.com/user/list" -H "X-Api-Path: user/list"
---

## Others:

[Examples](https://github.com/codewithyou365/idea_plugin_for_java/tree/main/src/main/java/org/codewithyou365/easyjava/example)

