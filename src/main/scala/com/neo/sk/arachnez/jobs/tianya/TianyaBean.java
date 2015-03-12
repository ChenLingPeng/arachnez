package com.neo.sk.arachnez.jobs.tianya;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/9/7
 * Time: 14:07
 * <p/>
 * //                            _ooOoo_
 * //                           o8888888o
 * //                           88" . "88
 * //                           (| -_- |)
 * //                            O\ = /O
 * //                        ____/`---'\____
 * //                      .   ' \\| |// `.
 * //                       / \\||| : |||// \
 * //                     / _||||| -:- |||||- \
 * //                       | | \\\ - /// | |
 * //                     | \_| ''\---/'' | |
 * //                      \ .-\__ `-` ___/-. /
 * //                   ___`. .' /--.--\ `. . __
 * //                ."" '< `.___\_<|>_/___.' >'"".
 * //               | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * //                 \ \ `-. \_ __\ /__ _/ .-` / /
 * //         ======`-.____`-.___\_____/___.-`____.-'======
 * //                            `=---='
 * //
 * //         .............................................
 * //
 * //   █████▒█    ██  ▄████▄   ██ ▄█▀       ██████╗ ██╗   ██╗ ██████╗
 * // ▓██   ▒ ██  ▓██▒▒██▀ ▀█   ██▄█▒        ██╔══██╗██║   ██║██╔════╝
 * // ▒████ ░▓██  ▒██░▒▓█    ▄ ▓███▄░        ██████╔╝██║   ██║██║  ███╗
 * // ░▓█▒  ░▓▓█  ░██░▒▓▓▄ ▄██▒▓██ █▄        ██╔══██╗██║   ██║██║   ██║
 * // ░▒█░   ▒▒█████▓ ▒ ▓███▀ ░▒██▒ █▄       ██████╔╝╚██████╔╝╚██████╔╝
 * //  ▒ ░   ░▒▓▒ ▒ ▒ ░ ░▒ ▒  ░▒ ▒▒ ▓▒       ╚═════╝  ╚═════╝  ╚═════╝
 * //  ░     ░░▒░ ░ ░   ░  ▒   ░ ░▒ ▒░
 * //  ░ ░    ░░░ ░ ░ ░        ░ ░░ ░
 * //           ░     ░ ░      ░  ░
 * //
 */
public class TianyaBean {
  private static final String SEP = "\u0001";
  private String boardName;
  private long postNum;
  private long floor; // 0 for main post
  private long tofloor; // 0 if to main post
  private long ctime;
  private String content;
  private String author;
  private String title;

  public TianyaBean(String boardName, String title, long postNum) {
    this.boardName = boardName;
    this.title = title;
    this.postNum = postNum;
  }

  @Override
  public String toString() {
    return boardName+SEP+postNum+SEP+floor+SEP+tofloor+SEP+ctime+SEP+title+SEP+author+SEP+content;
  }

  public long getFloor() {

    return floor;
  }

  public void setFloor(long floor) {
    this.floor = floor;
  }

  public long getTofloor() {
    return tofloor;
  }

  public void setTofloor(long tofloor) {
    this.tofloor = tofloor;
  }

  public long getCtime() {
    return ctime;
  }

  public void setCtime(long ctime) {
    this.ctime = ctime;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
