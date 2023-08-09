详细说明请看Report，运行结果在report的appendix里

运行需要将IDE的java运行版本设置在java8
并且本地的terminal的java版本也需要设置在java8，不然跑不起来
用intelij iead打开这个project
在terminal里输入：./gradlew clean deployNodes 配置节点
结束后输入：./build/nodes/runnodes 启动节点和测试环境
（有时某个节点可能会崩溃，如果你发现哪个窗口自己关掉了就把所有都关了
然后重新 ./build/nodes/runnodes）
这个project里其他需要提前配置的东西（比如节点自己的paillier key的生成）已经都弄好了可以直接跑，这个project里的readme里有一些corda模板的配置说明但大部分这里都用不到
如果配置运行的时候出现问题有时是本地网络环境的问题，比如什么进程把节点占用了得重启一下，也可能是和其他的什么东西有冲突，
如果本地电脑一直配置不起来就去linux上跑，我在学校dice上跑测试的时候就没有遇到过windows上遇到的很多问题了。

示例flow调用流程如下，运行的时候就复制粘贴再改改数值就行：

在user窗口下依次输入：
start createToken msg: "valid", amount: 4     用来来生成token，token需要大于4不然不够一个任务，msg必须是valid
start sendToken id: x, amount: 4, receiver: platform 用来发送token到主平台，这里的id是之前创建token时返回的tokenId
start requestFlow task: "valid", tokenId: x 用来发送请求，tokenId是上面send的token的id

在platform窗口下：
start spread id: x 广播任务给子平台，x是requestFlow返回的id，
也可以在主平台调用run vaultQuery contractStateType: com.template.states.request 找到

在子平台的窗口（比如supplier2）下：
start acceptTask id: x 接受任务，id是上一步返回的，也可以用run vaultQuery contractStateType: com.template.states.data 找到
start assignTask receivers: ["worker1"], taskId: x 分割任务为多个子任务并广播给workers，receivers就是他的workers的集合，taskId就是他所接受的任务的id

在工作者的窗口：
start acceptWork id: a3949ede-91e1-459a-b0be-1723f09bf714, receiver: supplier2 接受任务，receiver是他们各自所归属的子平台，上面assianTask谁发的这里就是谁
start returnResult receiver: supplier2, id: x, result1: 80, result2: 80, key_name: key2 返回任务结果，key_name是子平台的paillier key的对应名字，这里supplier2的是key2

子平台窗口下：
start returnTaskResult receiver: platform, id: x, works: ["502c95a2-7ef1-410d-8660-0714f5e367fa","a3949ede-91e1-459a-b0be-1723f09bf714"], caller: key2
在两个子任务都完成后返回总的任务结果给主平台

主平台窗口下：
start verify id: x, caller: platform 主平台验证并发送token奖励，这里的id是上面returntaskResult里返回结果所对应的id

在子平台：
start sendWage tokenId: 17d05789-291f-469e-bda8-38f5085e7b14, taskId: 502c95a2-7ef1-410d-8660-0714f5e367fa 发送奖励给工作者，taskid是上面assignTask所分割的子任务的id
，也是工作者在returnResult里返回的子任务结果所对应的子任务id
