package cn.an0nymous.chatroom.Bean;

public class VersionBean {
	private String url;// apk下载地址
	private String version;//服务器版本号
	private String desc;//更新信息

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "UrlBean [url=" + url + ", version=" + version
				+ ", desc=" + desc + "]";
	}

}
